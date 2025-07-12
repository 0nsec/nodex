package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Context information for message processing.
 */
@NotNullByDefault
public interface MessageContext {
    /**
     * Returns the message being processed.
     */
    Message getMessage();
    
    /**
     * Returns the group the message belongs to.
     */
    Group getGroup();
    
    /**
     * Returns the timestamp when the message was processed.
     */
    long getTimestamp();
    
    /**
     * Returns whether the message should be delivered.
     */
    boolean shouldDeliver();
    
    /**
     * Returns whether the message should be shared with other contacts.
     */
    boolean shouldShare();
}
