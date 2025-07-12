package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Factory for creating sync messages.
 */
@Immutable
@NotNullByDefault
public interface MessageFactory {

    /**
     * Creates a new message with the given content.
     */
    Message createMessage(GroupId groupId, long timestamp, byte[] body);

    /**
     * Creates a new message with the given content and dependencies.
     */
    Message createMessage(GroupId groupId, long timestamp, byte[] body, MessageId... dependencies);
}
