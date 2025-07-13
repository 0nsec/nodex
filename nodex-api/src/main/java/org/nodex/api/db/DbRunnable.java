package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A runnable interface for database operations.
 */
@NotNullByDefault
public interface DbRunnable<E extends Exception> {
    void run(Transaction txn) throws DbException, E;
}
