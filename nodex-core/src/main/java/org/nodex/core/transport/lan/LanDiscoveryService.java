package org.nodex.core.transport.lan;

import org.nodex.api.contact.ContactId;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Discovers peers on the local network.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class LanDiscoveryService implements Service {

    private static final Logger LOG = Logger.getLogger(LanDiscoveryService.class.getName());
    
    private volatile boolean started = false;

    @Inject
    public LanDiscoveryService() {
    }

    @Override
    public void startService() throws ServiceException {
        started = true;
        LOG.info("LAN discovery service started");
    }

    @Override
    public void stopService() throws ServiceException {
        started = false;
        LOG.info("LAN discovery service stopped");
    }

    public void poll(Collection<ContactId> connectedContacts) {
        // Poll for peers
    }
}
