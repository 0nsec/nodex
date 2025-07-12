package org.nodex.api.messaging.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class PrivateMessageReceivedEvent
		extends ConversationMessageReceivedEvent<PrivateMessageHeader> {
	public PrivateMessageReceivedEvent(PrivateMessageHeader messageHeader,
			ContactId contactId) {
		super(messageHeader, contactId);
	}
}