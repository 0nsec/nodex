package org.nodex.api.db;

/**
 * Exception thrown when a message is not found in the database.
 */
public class NoSuchMessageException extends DbException {
    public NoSuchMessageException(String message) {
        super(message);
    }

    public NoSuchMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchMessageException(Throwable cause) {
        super(cause);
    }
}
