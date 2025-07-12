package org.nodex.api.conversation.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ConversationMessageTrackedEvent extends Event {
	private final long timestamp;
	private final boolean read;
	private final ContactId contactId;
	public ConversationMessageTrackedEvent(long timestamp,
			boolean read, ContactId contactId) {
		this.timestamp = timestamp;
		this.read = read;
		this.contactId = contactId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public boolean getRead() {
		return read;
	}
	public ContactId getContactId() {
		return contactId;
	}
}