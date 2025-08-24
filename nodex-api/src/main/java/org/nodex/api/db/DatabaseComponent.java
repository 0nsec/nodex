package org.nodex.api.db;

import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.Collections;

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
     * Start a transaction.
     */
    Transaction startTransaction(boolean readOnly) throws DbException;
    
    /**
     * Commit a transaction.
     */
    void commitTransaction(Transaction txn) throws DbException;
    
    /**
     * End a transaction.
     */
    void endTransaction(Transaction txn);
    
    /**
     * Execute a transaction with result.
     */
    <R, E extends Exception> R transactionWithResult(boolean readOnly, DbCallable<R, E> callable) throws DbException, E;
    
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
     * Delete a message.
     */
    void deleteMessage(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
    
    /**
     * Delete message metadata.
     */
    void deleteMessageMetadata(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException;
    
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
    
    /**
     * Get a group by ID.
     */
    org.nodex.api.sync.Group getGroup(Transaction txn, org.nodex.api.sync.GroupId groupId) throws DbException;
    
    /**
     * Get all groups for a client.
     */
    java.util.Collection<org.nodex.api.sync.Group> getGroups(Transaction txn, org.nodex.api.sync.ClientId clientId, int majorVersion) throws DbException;

    // Compatibility shims
    default void removeMessage(Transaction txn, org.nodex.api.sync.MessageId messageId) throws DbException {
        deleteMessage(txn, messageId);
        try { deleteMessageMetadata(txn, messageId); } catch (Exception ignored) {}
    }
    default Collection<org.nodex.api.sync.MessageId> getMessageIds(Transaction txn, org.nodex.api.sync.GroupId groupId) { return Collections.emptyList(); }
    default Collection<org.nodex.api.sync.MessageStatus> getMessageStatus(Transaction txn, org.nodex.api.contact.ContactId contactId, org.nodex.api.sync.GroupId groupId) { return Collections.emptyList(); }
    default boolean containsContact(Transaction txn, org.nodex.api.identity.AuthorId a1, org.nodex.api.identity.AuthorId a2) { return false; }
    default <R, E extends Exception> R transactionWithNullableResult(boolean readOnly, DbCallable<R,E> callable) throws DbException, E { return transactionWithResult(readOnly, callable); }
}
