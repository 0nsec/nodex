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

    /**
     * Status of sharing between contacts.
     */
    enum SharingStatus {
        /**
         * Content is shareable with the contact.
         */
        SHAREABLE,
        
        /**
         * Content is being shared with the contact.
         */
        SHARING,
        
        /**
         * Content has been shared with the contact.
         */
        SHARED,
        
        /**
         * Content sharing was rejected by the contact.
         */
        REJECTED,
        
        /**
         * Content cannot be shared with the contact.
         */
        NOT_SHAREABLE
    }
}
