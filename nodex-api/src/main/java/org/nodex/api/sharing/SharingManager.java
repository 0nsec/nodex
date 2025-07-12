package org.nodex.api.sharing;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * Manager for sharing shareable objects
 */
@NotNullByDefault
public interface SharingManager<T extends Shareable> {
    
    void sendInvitation(ContactId contactId, T shareable) throws DbException;
    
    void sendInvitation(Transaction txn, ContactId contactId, T shareable) throws DbException;
    
    Collection<T> getSharedItems() throws DbException;
    
    Collection<T> getSharedItems(Transaction txn) throws DbException;
}
