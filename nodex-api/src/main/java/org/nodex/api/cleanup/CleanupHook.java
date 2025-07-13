package org.nodex.api.cleanup;

import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface CleanupHook {
    
    void performCleanup(Transaction txn) throws DbException;
}
