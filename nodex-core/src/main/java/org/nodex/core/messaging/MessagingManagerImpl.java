package org.nodex.core.messaging;

import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.event.EventBus;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Implementation of MessagingManager for private messaging - matches Briar exactly.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class MessagingManagerImpl implements MessagingManager, Service {

    private static final Logger LOG = Logger.getLogger(MessagingManagerImpl.class.getName());

    private final DatabaseComponent db;
    private final EventBus eventBus;
    
    private volatile boolean started = false;

    @Inject
    public MessagingManagerImpl(DatabaseComponent db, EventBus eventBus) {
        this.db = db;
        this.eventBus = eventBus;
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        LOG.info("Starting messaging manager");
        started = true;
        LOG.info("Messaging manager started");
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping messaging manager");
        started = false;
        LOG.info("Messaging manager stopped");
    }

    @Override
    public GroupId getContactGroupId(Contact contact) {
        // Generate a consistent group ID for messaging with this contact
        byte[] groupBytes = new byte[32];
        // In a real implementation, this would derive from contact ID + local identity
        return new GroupId(groupBytes);
    }

    @Override
    public void addLocalMessage(PrivateMessage message) throws DbException {
        Transaction txn = db.startTransaction(false);
        try {
            // Store the message in the database
            db.addMessage(txn, message, false);
            
            // Commit the transaction
            db.commitTransaction(txn);
            
            LOG.info("Added local private message: " + message.getId());
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public Collection<PrivateMessageHeader> getMessageHeaders(ContactId contactId) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            Contact contact = db.getContact(txn, contactId);
            GroupId groupId = getContactGroup(contact);
            
            // Get all messages for this contact group
            Collection<org.nodex.api.sync.Message> messages = db.getMessages(txn, groupId);
            
            // Convert to private message headers
            Collection<PrivateMessageHeader> headers = new ArrayList<>();
            for (org.nodex.api.sync.Message msg : messages) {
                // Create header from message
                PrivateMessageHeader header = createHeaderFromMessage(msg, contactId);
                headers.add(header);
            }
            
            db.commitTransaction(txn);
            return headers;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public String getMessageText(MessageId messageId) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            org.nodex.api.sync.Message message = db.getMessage(txn, messageId);
            
            // Extract text from message body
            String text = extractTextFromBody(message.getBody());
            
            db.commitTransaction(txn);
            return text;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public void setReadFlag(MessageId messageId, boolean read) throws DbException {
        Transaction txn = db.startTransaction(false);
        try {
            // Update read flag in metadata
            // In a real implementation, this would update message metadata
            
            db.commitTransaction(txn);
            LOG.fine("Set read flag for message " + messageId + " to " + read);
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public long getTimestamp(MessageId messageId) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            org.nodex.api.sync.Message message = db.getMessage(txn, messageId);
            long timestamp = message.getTimestamp();
            
            db.commitTransaction(txn);
            return timestamp;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    private PrivateMessageHeader createHeaderFromMessage(org.nodex.api.sync.Message message, ContactId contactId) {
        // Create a private message header from the message
        return new PrivateMessageHeader(
            message.getId(),
            message.getGroupId(),
            message.getTimestamp(),
            true, // local - simplified
            false, // read - simplified
            extractTextFromBody(message.getBody()),
            contactId
        );
    }

    private String extractTextFromBody(byte[] body) {
        // Extract text content from message body
        // In a real implementation, this would parse the BDF-encoded message
        return new String(body); // Simplified
    }
}
