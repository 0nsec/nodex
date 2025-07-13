package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

@NotNullByDefault
public interface TransactionManager extends Service {
    
    /**
     * Run a transaction with a result.
     */
    <E extends Exception> void transaction(boolean readOnly, 
                                         DbCallable<Void, E> task) throws DbException, E;
    
    /**
     * Run a transaction with a result.
     */
    <R, E extends Exception> R transactionWithResult(boolean readOnly,
                                                    DbCallable<R, E> task) throws DbException, E;
}
