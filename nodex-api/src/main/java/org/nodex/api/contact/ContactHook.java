package org.nodex.api.contact;

import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ContactHook {
    
    void addingContact(Transaction txn, Contact contact) throws DbException;
    
    void removingContact(Transaction txn, Contact contact) throws DbException;
}
