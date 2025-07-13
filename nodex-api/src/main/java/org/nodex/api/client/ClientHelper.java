package org.nodex.api.client;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Helper class for client operations.
 */
@NotNullByDefault
public interface ClientHelper {
    /**
     * Create a new session ID.
     */
    SessionId createSessionId();
    
    /**
     * Create a message ID.
     */
    org.nodex.api.sync.MessageId createMessageId();
    
    /**
     * Get contact ID for a group.
     */
    org.nodex.api.contact.ContactId getContactId(org.nodex.api.db.Transaction txn, org.nodex.api.sync.GroupId groupId) throws org.nodex.api.db.DbException;
    
    /**
     * Set contact ID for a group.
     */
    void setContactId(org.nodex.api.db.Transaction txn, org.nodex.api.sync.GroupId groupId, org.nodex.api.contact.ContactId contactId) throws org.nodex.api.db.DbException;
    
    /**
     * Create a message.
     */
    org.nodex.api.sync.Message createMessage(org.nodex.api.sync.GroupId groupId, long timestamp, org.nodex.api.data.BdfList body) throws org.nodex.api.FormatException;
    
    /**
     * Create a message from byte array.
     */
    org.nodex.api.sync.Message createMessage(org.nodex.api.sync.GroupId groupId, long timestamp, byte[] body) throws org.nodex.api.FormatException;
    
    /**
     * Convert BdfList to byte array.
     */
    byte[] toByteArray(org.nodex.api.data.BdfList list) throws org.nodex.api.FormatException;
    
    /**
     * Get message IDs matching query.
     */
    java.util.Collection<org.nodex.api.sync.MessageId> getMessageIds(org.nodex.api.db.Transaction txn, org.nodex.api.sync.GroupId groupId, org.nodex.api.data.BdfDictionary query) throws org.nodex.api.db.DbException;
    
    /**
     * Get message metadata as dictionary.
     */
    java.util.Collection<org.nodex.api.data.BdfDictionary> getMessageMetadataAsDictionary(org.nodex.api.db.Transaction txn, org.nodex.api.sync.GroupId groupId, org.nodex.api.data.BdfDictionary query) throws org.nodex.api.db.DbException;
    
    /**
     * Add local message.
     */
    void addLocalMessage(org.nodex.api.db.Transaction txn, org.nodex.api.sync.Message message, org.nodex.api.data.BdfDictionary metadata, boolean shared, boolean temporary) throws org.nodex.api.db.DbException;
}
