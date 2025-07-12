package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents the status of a message in the synchronization system.
 */
@NotNullByDefault
public enum MessageStatus {
    /**
     * The message is pending and has not been sent yet.
     */
    PENDING,
    
    /**
     * The message has been sent successfully.
     */
    SENT,
    
    /**
     * The message has been delivered to the recipient.
     */
    DELIVERED,
    
    /**
     * The message has been read by the recipient.
     */
    READ,
    
    /**
     * The message failed to send.
     */
    FAILED,
    
    /**
     * The message has been received.
     */
    RECEIVED
}
