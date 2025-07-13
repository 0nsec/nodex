package org.nodex.api.db;

import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface TransactionExtensions {
    
    void attach(Event event);
    
    void transaction(boolean readOnly, TransactionTask task) throws DbException;
    
    @FunctionalInterface
    interface TransactionTask {
        void run(Transaction txn) throws DbException;
    }
}
