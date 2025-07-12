package org.nodex.api.messaging;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;

import java.util.Collection;

/**
 * Interface for managing messaging operations
 */
@NotNullByDefault
public interface MessagingManager {
    
    /**
     * Client ID for the messaging system.
     */
    String CLIENT_ID = "org.nodex.client.messaging";
    
    /**
     * Major version of the messaging client.
     */
    int MAJOR_VERSION = 1;
    
    /**
     * Minor version of the messaging client.
     */
    int MINOR_VERSION = 0;
    
    /**
     * Sends a message to a contact
     */
    void sendMessage(Transaction txn, ContactId contactId, String messageText) throws DbException;
    
    /**
     * Gets all messages in a conversation with a contact
     */
    Collection<Message> getMessages(Transaction txn, ContactId contactId) throws DbException;
    
    /**
     * Gets all messages in a specific group
     */
    Collection<Message> getMessages(Transaction txn, GroupId groupId) throws DbException;
    
    /**
     * Marks a message as read
     */
    void markMessageAsRead(Transaction txn, ContactId contactId, long timestamp) throws DbException;
    
    /**
     * Gets the number of unread messages from a contact
     */
    int getUnreadMessageCount(Transaction txn, ContactId contactId) throws DbException;
}
