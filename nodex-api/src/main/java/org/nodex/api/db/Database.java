package org.nodex.api.db;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Database interface for performing database operations.
 */
@NotNullByDefault
public interface Database {
    /**
     * Start a new transaction.
     */
    Transaction startTransaction();
    
    /**
     * Execute a query.
     */
    <T> T executeQuery(String query, Object... params);
    
    /**
     * Execute an update statement.
     */
    int executeUpdate(String query, Object... params);
}
