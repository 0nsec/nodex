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
}
