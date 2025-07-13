package org.nodex.core.transport.lan;

import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Server socket for accepting incoming LAN connections.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class LanServerSocket implements Service {

    private static final Logger LOG = Logger.getLogger(LanServerSocket.class.getName());
    
    private volatile boolean started = false;

    @Inject
    public LanServerSocket() {
    }

    @Override
    public void startService() throws ServiceException {
        started = true;
        LOG.info("LAN server socket started");
    }

    @Override
    public void stopService() throws ServiceException {
        started = false;
        LOG.info("LAN server socket stopped");
    }
}
