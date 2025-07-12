package org.nodex.core.api.db;

/**
 * Exception thrown when a database operation fails.
 */
public class DbException extends Exception {
    public DbException() {
        super();
    }
    
    public DbException(String message) {
        super(message);
    }
    
    public DbException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DbException(Throwable cause) {
        super(cause);
    }
}
