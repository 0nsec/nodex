package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A callable interface for database operations.
 */
@NotNullByDefault
public interface DbCallable<R, E extends Exception> {
    R call() throws DbException, E;
}
