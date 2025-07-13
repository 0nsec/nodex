package org.nodex.api.lifecycle;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Exception thrown when a service operation fails.
 */
@NotNullByDefault
public class ServiceException extends Exception {

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
}
