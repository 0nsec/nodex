package org.nodex.api.conversation.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ConversationMessageReceivedEvent<H extends ConversationMessageHeader>
		extends Event {
	private final H messageHeader;
	private final ContactId contactId;
	public ConversationMessageReceivedEvent(H messageHeader,
			ContactId contactId) {
		this.messageHeader = messageHeader;
		this.contactId = contactId;
	}
	public H getMessageHeader() {
		return messageHeader;
	}
	public ContactId getContactId() {
		return contactId;
	}
}