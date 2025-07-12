package org.nodex.api.privategroup.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.privategroup.Visibility;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ContactRelationshipRevealedEvent extends Event {
	private final GroupId groupId;
	private final AuthorId memberId;
	private final ContactId contactId;
	private final Visibility visibility;
	public ContactRelationshipRevealedEvent(GroupId groupId, AuthorId memberId,
			ContactId contactId, Visibility visibility) {
		this.groupId = groupId;
		this.memberId = memberId;
		this.contactId = contactId;
		this.visibility = visibility;
	}
	public GroupId getGroupId() {
		return groupId;
	}
	public AuthorId getMemberId() {
		return memberId;
	}
	public ContactId getContactId() {
		return contactId;
	}
	public Visibility getVisibility() {
		return visibility;
	}
}