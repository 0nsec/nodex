package org.nodex.api.db;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Database component for managing database connections and operations.
 */
@NotNullByDefault
public interface DatabaseComponent {
    /**
     * Get the database connection.
     */
    Database getDatabase();
    
    /**
     * Create a new transaction.
     */
    Transaction createTransaction();
}
