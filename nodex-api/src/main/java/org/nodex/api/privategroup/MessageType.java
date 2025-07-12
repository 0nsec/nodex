package org.nodex.api.privategroup;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Types of messages in private groups.
 */
@NotNullByDefault
public enum MessageType {
    /**
     * A message when a member joins the group.
     */
    JOIN,
    
    /**
     * A regular post message.
     */
    POST
}
