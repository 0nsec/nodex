package org.nodex.core.api.db;

/**
 * Exception thrown when trying to access a message that doesn't exist.
 */
public class NoSuchMessageException extends DbException {
    public NoSuchMessageException() {
        super();
    }
    
    public NoSuchMessageException(String message) {
        super(message);
    }
}
