package org.nodex.api.sync.validation;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for incoming message hook behavior.
 */
@NotNullByDefault
public class IncomingMessageHookConstants {
    
    /**
     * Delivery actions that can be taken for incoming messages.
     */
    public enum DeliveryAction {
        /**
         * Accept the message but do not share it with other contacts.
         */
        ACCEPT_DO_NOT_SHARE,
        
        /**
         * Accept the message and share it with other contacts.
         */
        ACCEPT_SHARE,
        
        /**
         * Reject the message.
         */
        REJECT
    }
    
    private IncomingMessageHookConstants() {
        // Utility class
    }
}
