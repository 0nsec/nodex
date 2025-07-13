package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Functional interface for database operations.
 */
@NotNullByDefault
@FunctionalInterface
public interface DbCallable<R, E extends Exception> {
    
    /**
     * Execute the database operation.
     */
    R call(Transaction txn) throws DbException, E;
}
