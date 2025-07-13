package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Exception thrown by transport operations.
 */
@NotNullByDefault
public class TransportException extends Exception {
    
    public TransportException() {
        super();
    }
    
    public TransportException(String message) {
        super(message);
    }
    
    public TransportException(Throwable cause) {
        super(cause);
    }
    
    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
