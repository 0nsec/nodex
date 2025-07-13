package org.nodex.api.db;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Database component for managing database connections and operations.
 */
@NotNullByDefault
public interface DatabaseComponent {
    /**
     * Get the database connection.
     */
    Database getDatabase();
    
    /**
     * Create a new transaction.
     */
    Transaction createTransaction();
    
    /**
     * Execute a transaction with result.
     */
    <T> T transactionWithResult(boolean readOnly, DbCallable<T, DbException> callable) throws DbException;
    
    /**
     * Execute a transaction.
     */
    void transaction(boolean readOnly, DbRunnable<DbException> runnable) throws DbException;
    
    /**
     * Get a contact by ID.
     */
    org.nodex.api.contact.Contact getContact(Transaction txn, org.nodex.api.contact.ContactId contactId) throws DbException;
    
    /**
     * Get all contacts.
     */
    java.util.Collection<org.nodex.api.contact.Contact> getContacts(Transaction txn) throws DbException;
    
    /**
     * Check if a group exists.
     */
    boolean containsGroup(Transaction txn, org.nodex.api.sync.GroupId groupId) throws DbException;
    
    /**
     * Add a group.
     */
    void addGroup(Transaction txn, org.nodex.api.sync.Group group) throws DbException;
    
    /**
     * Remove a group.
     */
    void removeGroup(Transaction txn, org.nodex.api.sync.Group group) throws DbException;
    
    /**
     * Set group visibility.
     */
    void setGroupVisibility(Transaction txn, org.nodex.api.contact.ContactId contactId, 
        org.nodex.api.sync.GroupId groupId, org.nodex.api.sync.Visibility visibility) throws DbException;
    
    /**
     * Start cleanup timer for a message.
     */
    void startCleanupTimer(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
    
    /**
     * Stop cleanup timer for a message.
     */
    void stopCleanupTimer(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
    
    /**
     * Set cleanup timer duration.
     */
    void setCleanupTimerDuration(Transaction txn, org.nodex.api.sync.MessageId messageId, long duration) throws DbException;
    
    /**
     * Set message as shared.
     */
    void setMessageShared(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
    
    /**
     * Set message as permanent.
     */
    void setMessagePermanent(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
}
