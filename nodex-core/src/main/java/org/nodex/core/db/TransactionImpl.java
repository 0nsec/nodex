package org.nodex.core.db;

import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Connection;

/**
 * Implementation of Transaction.
 */
@ThreadSafe
@NotNullByDefault
public class TransactionImpl implements Transaction {

    private final long transactionId;
    private final Connection connection;
    private boolean committed = false;
    private boolean aborted = false;

    public TransactionImpl(long transactionId, Connection connection) {
        this.transactionId = transactionId;
        this.connection = connection;
    }

    @Override
    public long getId() {
        return transactionId;
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Transaction connection is null");
        }
        return connection;
    }

    public void markCommitted() {
        if (aborted) {
            throw new IllegalStateException("Transaction already aborted");
        }
        committed = true;
    }

    public void markAborted() {
        if (committed) {
            throw new IllegalStateException("Transaction already committed");
        }
        aborted = true;
    }

    public boolean isCommitted() {
        return committed;
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean isActive() {
        return !committed && !aborted;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TransactionImpl)) return false;
        TransactionImpl other = (TransactionImpl) obj;
        return transactionId == other.transactionId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + "}";
    }
}
