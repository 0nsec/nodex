package org.nodex.api.privategroup.invitation;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;

import javax.annotation.concurrent.Immutable;

/**
 * Represents a group invitation item.
 */
@Immutable
@NotNullByDefault
public class GroupInvitationItem {

    private final GroupId groupId;
    private final ContactId contactId;
    private final String text;
    private final long timestamp;
    private final boolean canBeOpened;

    public GroupInvitationItem(GroupId groupId, ContactId contactId, String text, long timestamp, boolean canBeOpened) {
        this.groupId = groupId;
        this.contactId = contactId;
        this.text = text;
        this.timestamp = timestamp;
        this.canBeOpened = canBeOpened;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public ContactId getContactId() {
        return contactId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean canBeOpened() {
        return canBeOpened;
    }
}
