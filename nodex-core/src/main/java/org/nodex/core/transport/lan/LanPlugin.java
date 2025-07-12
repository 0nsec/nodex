package org.nodex.core.transport.lan;

import org.nodex.api.plugin.TransportPlugin;
import org.nodex.api.transport.TransportId;
import org.nodex.api.transport.TransportKeys;
import org.nodex.api.transport.TransportProperties;
import org.nodex.api.contact.ContactId;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * LAN transport plugin for local network discovery - matches Briar's LanPlugin.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class LanPlugin implements TransportPlugin, Service {

    private static final Logger LOG = Logger.getLogger(LanPlugin.class.getName());
    
    public static final TransportId ID = new TransportId("lan");
    
    // LAN configuration constants matching Briar
    private static final int MAX_FRAME_LENGTH = 1024 * 32; // 32KB for LAN
    private static final int CONNECTION_TIMEOUT_MS = 60_000; // 1 minute
    private static final int DISCOVERY_INTERVAL_MS = 30_000; // 30 seconds
    private static final int DEFAULT_PORT = 7916; // Briar's default LAN port
    
    private final LanConnectionManager connectionManager;
    private final LanDiscoveryService discoveryService;
    private final LanServerSocket serverSocket;
    
    private volatile boolean started = false;
    private final CopyOnWriteArrayList<TransportProperties> supportedProperties = new CopyOnWriteArrayList<>();

    @Inject
    public LanPlugin(LanConnectionManager connectionManager,
                    LanDiscoveryService discoveryService,
                    LanServerSocket serverSocket) {
        this.connectionManager = connectionManager;
        this.discoveryService = discoveryService;
        this.serverSocket = serverSocket;
        
        // Initialize supported properties
        initializeSupportedProperties();
    }

    @Override
    public TransportId getId() {
        return ID;
    }

    @Override
    public int getMaxFrameLength() {
        return MAX_FRAME_LENGTH;
    }

    @Override
    public int getMaxLatency() {
        return CONNECTION_TIMEOUT_MS;
    }

    @Override
    public int getMaxIdleTime() {
        return CONNECTION_TIMEOUT_MS * 3;
    }

    @Override
    public boolean shouldPoll() {
        return true; // LAN requires polling for peer discovery
    }

    @Override
    public int getPollingInterval() {
        return DISCOVERY_INTERVAL_MS;
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        LOG.info("Starting LAN transport plugin");
        
        try {
            // Start server socket for incoming connections
            serverSocket.startService();
            
            // Start discovery service for finding peers
            discoveryService.startService();
            
            // Start connection manager
            connectionManager.startService();
            
            started = true;
            LOG.info("LAN transport plugin started successfully on port " + DEFAULT_PORT);
        } catch (Exception e) {
            throw new ServiceException("Failed to start LAN transport", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping LAN transport plugin");
        
        try {
            // Stop discovery first
            discoveryService.stopService();
            
            // Stop connection manager
            connectionManager.stopService();
            
            // Stop server socket last
            serverSocket.stopService();
            
            started = false;
            LOG.info("LAN transport plugin stopped successfully");
        } catch (Exception e) {
            throw new ServiceException("Failed to stop LAN transport", e);
        }
    }

    @Override
    public Collection<TransportProperties> getSupportedProperties() {
        return supportedProperties;
    }

    @Override
    public TransportKeys generateKeys() {
        // Generate LAN-specific transport keys
        return connectionManager.generateTransportKeys();
    }

    @Override
    public boolean supportsKeyAgreement() {
        return true; // LAN supports key agreement
    }

    @Override
    public boolean supportsRendezvous() {
        return true; // LAN supports rendezvous connections
    }

    @Override
    public void poll(Collection<ContactId> connectedContacts) {
        if (!started) return;
        
        // Trigger discovery and connection attempts
        discoveryService.poll(connectedContacts);
        connectionManager.poll(connectedContacts);
    }

    private void initializeSupportedProperties() {
        // Add LAN-specific transport properties
        supportedProperties.add(new TransportProperties("lan.port", String.valueOf(DEFAULT_PORT)));
        supportedProperties.add(new TransportProperties("lan.discovery.enabled", "true"));
        supportedProperties.add(new TransportProperties("lan.multicast.enabled", "true"));
        supportedProperties.add(new TransportProperties("lan.max_connections", "16"));
        supportedProperties.add(new TransportProperties("lan.connection_timeout", String.valueOf(CONNECTION_TIMEOUT_MS)));
    }
}
