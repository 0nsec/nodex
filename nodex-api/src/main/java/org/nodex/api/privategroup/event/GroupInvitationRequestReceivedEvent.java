package org.nodex.api.privategroup.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when a group invitation request is received.
 */
@Immutable
@NotNullByDefault
public class GroupInvitationRequestReceivedEvent extends Event {

    private final ContactId contactId;
    private final GroupId groupId;
    private final String text;

    public GroupInvitationRequestReceivedEvent(ContactId contactId, GroupId groupId, String text) {
        this.contactId = contactId;
        this.groupId = groupId;
        this.text = text;
    }

    public ContactId getContactId() {
        return contactId;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public String getText() {
        return text;
    }
}
