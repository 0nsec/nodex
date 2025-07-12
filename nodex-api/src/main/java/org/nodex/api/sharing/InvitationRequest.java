package org.nodex.api.sharing;

import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Base class for invitation requests
 */
@Immutable
@NotNullByDefault
public abstract class InvitationRequest<T extends Shareable> extends ConversationRequest<T> {
    private final boolean available;
    private final boolean canBeOpened;

    protected InvitationRequest(MessageId messageId, GroupId groupId,
                               long timestamp, boolean local, boolean read,
                               boolean sent, boolean seen, SessionId sessionId,
                               T shareable, @Nullable String text,
                               boolean available, boolean canBeOpened,
                               long autoDeleteTimer) {
        super(messageId, groupId, timestamp, local, read, sent, seen,
                sessionId, shareable, text, false, autoDeleteTimer);
        this.available = available;
        this.canBeOpened = canBeOpened;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean canBeOpened() {
        return canBeOpened;
    }
}
