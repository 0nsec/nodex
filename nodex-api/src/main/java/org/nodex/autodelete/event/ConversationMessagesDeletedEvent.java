package org.nodex.api.autodelete.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ConversationMessagesDeletedEvent extends Event {
	private final ContactId contactId;
	private final Collection<MessageId> messageIds;
	public ConversationMessagesDeletedEvent(ContactId contactId,
			Collection<MessageId> messageIds) {
		this.contactId = contactId;
		this.messageIds = messageIds;
	}
	public ContactId getContactId() {
		return contactId;
	}
	public Collection<MessageId> getMessageIds() {
		return messageIds;
	}
}