package org.nodex.api.client;

import org.nodex.nullsafety.NotNullByDefault;
import org.nodex.api.FormatException;
import org.nodex.api.contact.ContactId;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;

import java.util.Collection;
import java.util.Map;

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
    MessageId createMessageId();
    
    /**
     * Get contact ID for a group.
     */
    ContactId getContactId(Transaction txn, GroupId groupId) throws DbException;
    
    /**
     * Set contact ID for a group.
     */
    void setContactId(Transaction txn, GroupId groupId, ContactId contactId) throws DbException;
    
    /**
     * Create a message.
     */
    Message createMessage(GroupId groupId, long timestamp, BdfList body) throws FormatException;
    
    /**
     * Create a message from byte array.
     */
    Message createMessage(GroupId groupId, long timestamp, byte[] body) throws FormatException;
    
    /**
     * Convert BdfList to byte array.
     */
    byte[] toByteArray(BdfList list) throws FormatException;
    
    /**
     * Convert BdfDictionary to byte array.
     */
    byte[] toByteArray(BdfDictionary dictionary) throws FormatException;
    
    /**
     * Get message IDs matching query.
     */
    Collection<MessageId> getMessageIds(Transaction txn, GroupId groupId, BdfDictionary query) throws DbException;
    
    /**
     * Get message metadata as dictionary.
     */
    Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(Transaction txn, GroupId groupId, BdfDictionary query) throws DbException;
    
    /**
     * Get message metadata as dictionary for a specific message.
     */
    BdfDictionary getMessageMetadataAsDictionary(Transaction txn, MessageId messageId) throws DbException, FormatException;
    
    /**
     * Add local message.
     */
    void addLocalMessage(Transaction txn, Message message, BdfDictionary metadata, boolean shared, boolean temporary) throws DbException;
    
    // Additional methods needed by the core implementation
    
    /**
     * Get a message by ID.
     */
    Message getMessage(Transaction txn, MessageId messageId) throws DbException;
    
    /**
     * Get group metadata as dictionary.
     */
    BdfDictionary getGroupMetadataAsDictionary(Transaction txn, GroupId groupId) throws DbException, FormatException;
    
    /**
     * Merge group metadata.
     */
    void mergeGroupMetadata(Transaction txn, GroupId groupId, BdfDictionary metadata) throws DbException, FormatException;
    
    /**
     * Merge message metadata.
     */
    void mergeMessageMetadata(Transaction txn, MessageId messageId, BdfDictionary metadata) throws DbException, FormatException;
    
    /**
     * Convert byte array to BdfList.
     */
    BdfList toList(byte[] bytes) throws FormatException;
    
    /**
     * Convert byte array to BdfList with offset and length.
     */
    BdfList toList(byte[] bytes, int offset, int length) throws FormatException;
    
    /**
     * Convert Message to BdfList.
     */
    BdfList toList(Message message) throws FormatException;
    
    /**
     * Convert Author to BdfList.
     */
    BdfList toList(Author author);
    
    /**
     * Parse and validate an Author from BdfList.
     */
    Author parseAndValidateAuthor(BdfList authorList) throws FormatException;
    
    /**
     * Convert byte array to BdfDictionary.
     */
    BdfDictionary toDictionary(byte[] bytes) throws FormatException;
    
    /**
     * Convert byte array to BdfDictionary with offset and length.
     */
    BdfDictionary toDictionary(byte[] bytes, int offset, int length) throws FormatException;
    
    /**
     * Get message as BdfList.
     */
    BdfList getMessageAsList(MessageId messageId) throws DbException, FormatException;
    
    /**
     * Get message as BdfList (transaction version).
     */
    BdfList getMessageAsList(Transaction txn, MessageId messageId) throws DbException, FormatException;
    
    /**
     * Sign data with private key.
     */
    byte[] sign(String label, BdfList data, byte[] privateKey) throws FormatException;
    
    /**
     * Verify signature.
     */
    void verifySignature(byte[] signature, String label, BdfList data, byte[] publicKey) throws FormatException;
}
