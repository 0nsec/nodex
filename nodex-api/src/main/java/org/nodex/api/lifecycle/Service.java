package org.nodex.api.lifecycle;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base interface for all services in the NodeX system.
 */
@NotNullByDefault
public interface Service {
    
    /**
     * Start the service.
     */
    void startService() throws ServiceException;
    
    /**
     * Stop the service.
     */
    void stopService() throws ServiceException;
    
    /**
     * Check if the service is running.
     */
    boolean isRunning();
}
