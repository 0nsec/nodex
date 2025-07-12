package org.nodex.api.privategroup.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when a group invitation response is received.
 */
@Immutable
@NotNullByDefault
public class GroupInvitationResponseReceivedEvent extends Event {

    private final ContactId contactId;
    private final GroupId groupId;
    private final boolean accepted;

    public GroupInvitationResponseReceivedEvent(ContactId contactId, GroupId groupId, boolean accepted) {
        this.contactId = contactId;
        this.groupId = groupId;
        this.accepted = accepted;
    }

    public ContactId getContactId() {
        return contactId;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
