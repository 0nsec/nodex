package org.nodex.core.db;

import org.nodex.api.db.TransactionManager;
import org.nodex.api.db.Transaction;
import org.nodex.api.db.DbException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Implementation of TransactionManager that manages database transactions.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOG = Logger.getLogger(TransactionManagerImpl.class.getName());

    private final AtomicLong transactionIdCounter = new AtomicLong(0);

    @Inject
    public TransactionManagerImpl() {
    }

    @Override
    public Transaction startTransaction(boolean readOnly) throws DbException {
        long transactionId = transactionIdCounter.incrementAndGet();
        
        LOG.fine("Starting transaction " + transactionId + 
                (readOnly ? " (read-only)" : " (read-write)"));
        
        // Create a new transaction instance
        // In a real implementation, this would manage actual database connections
        return new TransactionImpl(transactionId, null); // Connection will be set by DatabaseComponent
    }

    @Override
    public void commitTransaction(Transaction transaction) throws DbException {
        if (!(transaction instanceof TransactionImpl)) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        
        TransactionImpl txn = (TransactionImpl) transaction;
        LOG.fine("Committing transaction " + txn.getId());
        
        // Mark transaction as committed
        txn.markCommitted();
    }

    @Override
    public void abortTransaction(Transaction transaction) throws DbException {
        if (!(transaction instanceof TransactionImpl)) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        
        TransactionImpl txn = (TransactionImpl) transaction;
        LOG.fine("Aborting transaction " + txn.getId());
        
        // Mark transaction as aborted
        txn.markAborted();
    }
}
