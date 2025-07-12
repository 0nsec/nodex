package org.nodex.api.sharing;

import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Base class for invitation responses
 */
@Immutable
@NotNullByDefault
public abstract class InvitationResponse extends ConversationResponse {
    protected InvitationResponse(MessageId messageId, GroupId groupId,
                                long timestamp, boolean local, boolean read,
                                boolean sent, boolean seen, SessionId sessionId,
                                boolean accepted, long autoDeleteTimer,
                                boolean isAutoDecline) {
        super(messageId, groupId, timestamp, local, read, sent, seen,
                sessionId, accepted, autoDeleteTimer, isAutoDecline);
    }
}
