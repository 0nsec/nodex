package org.nodex.api.privategroup.event;

import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when a group message is added.
 */
@Immutable
@NotNullByDefault
public class GroupMessageAddedEvent extends Event {

    private final GroupId groupId;
    private final MessageId messageId;

    public GroupMessageAddedEvent(GroupId groupId, MessageId messageId) {
        this.groupId = groupId;
        this.messageId = messageId;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public MessageId getMessageId() {
        return messageId;
    }
}
