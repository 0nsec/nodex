package org.nodex.api.privategroup.invitation;

import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

import javax.annotation.concurrent.Immutable;

/**
 * A response to a private group invitation.
 */
@Immutable
@NotNullByDefault
public class GroupInvitationResponse extends ConversationMessageHeader {

    private final boolean accepted;
    private final String message;

    public GroupInvitationResponse(MessageId id, GroupId groupId, long timestamp,
                                  boolean local, boolean read, boolean sent, boolean seen,
                                  long autoDeleteTimer, boolean accepted, String message) {
        super(id, groupId, timestamp, local, read, sent, seen, autoDeleteTimer);
        this.accepted = accepted;
        this.message = message;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitGroupInvitationResponse(this);
    }
}
