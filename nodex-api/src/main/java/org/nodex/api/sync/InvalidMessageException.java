package org.nodex.api.sync;

/**
 * Exception thrown when a message is invalid or cannot be processed.
 */
public class InvalidMessageException extends Exception {
    
    public InvalidMessageException() {
        super();
    }
    
    public InvalidMessageException(String message) {
        super(message);
    }
    
    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidMessageException(Throwable cause) {
        super(cause);
    }
}
