package org.nodex.api.privategroup.event;

import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when a private group is dissolved.
 */
@Immutable
@NotNullByDefault
public class GroupDissolvedEvent extends Event {

    private final GroupId groupId;

    public GroupDissolvedEvent(GroupId groupId) {
        this.groupId = groupId;
    }

    public GroupId getGroupId() {
        return groupId;
    }
}
