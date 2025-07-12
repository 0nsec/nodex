package org.nodex.api.db;

/**
 * Database transaction interface
 */
public interface Transaction {
    void commit() throws DbException;
    void abort() throws DbException;
}
