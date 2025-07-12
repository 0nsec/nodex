package org.nodex.api.db;

/**
 * Exception thrown when a database operation fails unexpectedly.
 */
public class UnexpectedTimerException extends DbException {
    public UnexpectedTimerException() {
        super();
    }

    public UnexpectedTimerException(String message) {
        super(message);
    }

    public UnexpectedTimerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedTimerException(Throwable cause) {
        super(cause);
    }
}
