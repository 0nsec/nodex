package org.nodex.api.db;

/**
 * Database exception for transactional operations
 */
public class DbException extends Exception {
    public DbException() {
        super("Database operation failed");
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
