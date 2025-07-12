package org.nodex.api.sharing.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ContactLeftShareableEvent extends Event {
	private final GroupId groupId;
	private final ContactId contactId;
	public ContactLeftShareableEvent(GroupId groupId, ContactId contactId) {
		this.groupId = groupId;
		this.contactId = contactId;
	}
	public GroupId getGroupId() {
		return groupId;
	}
	public ContactId getContactId() {
		return contactId;
	}
}