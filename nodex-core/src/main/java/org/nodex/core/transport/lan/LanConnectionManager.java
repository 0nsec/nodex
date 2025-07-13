package org.nodex.core.transport.lan;

import org.nodex.api.contact.ContactId;
import org.nodex.api.transport.TransportKeys;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages LAN connections to peers.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class LanConnectionManager implements Service {

    private static final Logger LOG = Logger.getLogger(LanConnectionManager.class.getName());
    
    private final ConcurrentHashMap<ContactId, LanConnection> activeConnections = new ConcurrentHashMap<>();
    private volatile boolean started = false;

    @Inject
    public LanConnectionManager() {
    }

    @Override
    public void startService() throws ServiceException {
        started = true;
        LOG.info("LAN connection manager started");
    }

    @Override
    public void stopService() throws ServiceException {
        activeConnections.clear();
        started = false;
        LOG.info("LAN connection manager stopped");
    }

    public TransportKeys generateTransportKeys() {
        byte[] keyData = new byte[32];
        return new TransportKeys(keyData);
    }

    public void poll(Collection<ContactId> connectedContacts) {
        // Poll for new connections
    }
}
