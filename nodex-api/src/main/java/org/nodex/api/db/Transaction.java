package org.nodex.api.db;

/**
 * Database transaction interface
 */
public interface Transaction {
    void commit() throws DbException;
    void abort() throws DbException;
    
    /**
     * Attach an event to this transaction.
     */
    void attach(org.nodex.api.event.Event event);
}
