package org.nodex.core.transport.bluetooth;

import org.nodex.api.plugin.TransportPlugin;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.transport.TransportKeys;
import org.nodex.api.properties.TransportProperties;
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
 * Bluetooth transport plugin for mesh networking - matches Briar's BluetoothPlugin.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class BluetoothPlugin implements TransportPlugin, Service {

    private static final Logger LOG = Logger.getLogger(BluetoothPlugin.class.getName());
    
    public static final TransportId ID = new TransportId("bluetooth");
    
    // Bluetooth configuration constants matching Briar
    private static final int MAX_FRAME_LENGTH = 1024;
    private static final int CONNECTION_TIMEOUT_MS = 30_000;
    private static final int DISCOVERY_INTERVAL_MS = 60_000;
    
    private final BluetoothConnectionManager connectionManager;
    private final BluetoothAdvertiser advertiser;
    private final BluetoothScanner scanner;
    
    private volatile boolean started = false;
    private final CopyOnWriteArrayList<TransportProperties> supportedProperties = new CopyOnWriteArrayList<>();

    @Inject
    public BluetoothPlugin(BluetoothConnectionManager connectionManager,
                          BluetoothAdvertiser advertiser,
                          BluetoothScanner scanner) {
        this.connectionManager = connectionManager;
        this.advertiser = advertiser;
        this.scanner = scanner;
        
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
        return CONNECTION_TIMEOUT_MS * 2;
    }

    @Override
    public boolean shouldPoll() {
        return true; // Bluetooth requires active polling for connections
    }

    @Override
    public int getPollingInterval() {
        return DISCOVERY_INTERVAL_MS;
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        LOG.info("Starting Bluetooth transport plugin");
        
        try {
            // Check if Bluetooth is available
            if (!isBluetoothAvailable()) {
                throw new ServiceException("Bluetooth not available on this device");
            }
            
            // Start connection manager
            connectionManager.startService();
            
            // Start advertising our presence
            advertiser.startService();
            
            // Start scanning for other devices
            scanner.startService();
            
            started = true;
            LOG.info("Bluetooth transport plugin started successfully");
        } catch (Exception e) {
            throw new ServiceException("Failed to start Bluetooth transport", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping Bluetooth transport plugin");
        
        try {
            // Stop scanner first
            scanner.stopService();
            
            // Stop advertiser
            advertiser.stopService();
            
            // Stop connection manager last
            connectionManager.stopService();
            
            started = false;
            LOG.info("Bluetooth transport plugin stopped successfully");
        } catch (Exception e) {
            throw new ServiceException("Failed to stop Bluetooth transport", e);
        }
    }

    @Override
    public Collection<TransportProperties> getSupportedProperties() {
        return supportedProperties;
    }

    @Override
    public TransportKeys generateKeys() {
        // Generate Bluetooth-specific transport keys
        return connectionManager.generateTransportKeys();
    }

    @Override
    public boolean supportsKeyAgreement() {
        return true; // Bluetooth supports key agreement
    }

    @Override
    public boolean supportsRendezvous() {
        return true; // Bluetooth supports rendezvous connections
    }

    @Override
    public void poll(Collection<ContactId> connectedContacts) {
        if (!started) return;
        
        // Trigger discovery and connection attempts
        scanner.poll(connectedContacts);
        connectionManager.poll(connectedContacts);
    }

    private boolean isBluetoothAvailable() {
        // Check if Bluetooth adapter is present and enabled
        try {
            // In a real implementation, this would check the system's Bluetooth adapter
            return true; // Simplified for now
        } catch (Exception e) {
            LOG.warning("Bluetooth availability check failed: " + e.getMessage());
            return false;
        }
    }

    private void initializeSupportedProperties() {
        // Add Bluetooth-specific transport properties
        supportedProperties.add(new TransportProperties("bluetooth.adapter.enabled", "true"));
        supportedProperties.add(new TransportProperties("bluetooth.discovery.enabled", "true"));
        supportedProperties.add(new TransportProperties("bluetooth.max_connections", "8"));
        supportedProperties.add(new TransportProperties("bluetooth.connection_timeout", String.valueOf(CONNECTION_TIMEOUT_MS)));
    }
}
