package org.nodex.api.sync.validation;

import org.nodex.api.db.DbException;
import org.nodex.api.db.Metadata;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;

/**
 * Hook interface for handling incoming messages.
 */
@NotNullByDefault
public interface IncomingMessageHook {
    
    /**
     * Called when a message is received.
     * 
     * @param txn The database transaction
     * @param message The received message
     * @param metadata The message metadata
     * @return The delivery action to take
     * @throws DbException if a database error occurs
     * @throws InvalidMessageException if the message is invalid
     */
    DeliveryAction incomingMessage(Transaction txn, Message message, Metadata metadata) 
            throws DbException, InvalidMessageException;
    
    /**
     * Defines the action to take for an incoming message.
     */
    enum DeliveryAction {
        /**
         * Accept the message and share it with contacts.
         */
        ACCEPT_SHARE,
        
        /**
         * Accept the message but do not share it.
         */
        ACCEPT_DO_NOT_SHARE,
        
        /**
         * Reject the message.
         */
        REJECT
    }
}
