package org.nodex.api.messaging.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class AttachmentReceivedEvent extends Event {
	private final MessageId messageId;
	private final ContactId contactId;
	public AttachmentReceivedEvent(MessageId messageId, ContactId contactId) {
		this.messageId = messageId;
		this.contactId = contactId;
	}
	public MessageId getMessageId() {
		return messageId;
	}
	public ContactId getContactId() {
		return contactId;
	}
}