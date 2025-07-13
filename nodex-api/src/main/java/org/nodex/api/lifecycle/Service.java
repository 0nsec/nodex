package org.nodex.api.lifecycle;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base interface for all services in the system.
 */
@NotNullByDefault
public interface Service {
    
    /**
     * Starts the service.
     * @throws ServiceException if the service cannot be started
     */
    void startService() throws ServiceException;
    
    /**
     * Stops the service.
     * @throws ServiceException if the service cannot be stopped
     */
    void stopService() throws ServiceException;
}
