package org.nodex.api.privategroup.invitation;

import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

import javax.annotation.concurrent.Immutable;

/**
 * A request to join a private group.
 */
@Immutable
@NotNullByDefault
public class GroupInvitationRequest extends ConversationMessageHeader {

    private final String groupName;
    private final String message;
    private final boolean canBeOpened;

    public GroupInvitationRequest(MessageId id, GroupId groupId, long timestamp,
                                 boolean local, boolean read, boolean sent, boolean seen,
                                 long autoDeleteTimer, String groupName, String message,
                                 boolean canBeOpened) {
        super(id, groupId, timestamp, local, read, sent, seen, autoDeleteTimer);
        this.groupName = groupName;
        this.message = message;
        this.canBeOpened = canBeOpened;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getMessage() {
        return message;
    }

    public boolean canBeOpened() {
        return canBeOpened;
    }

    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitGroupInvitationRequest(this);
    }
}
