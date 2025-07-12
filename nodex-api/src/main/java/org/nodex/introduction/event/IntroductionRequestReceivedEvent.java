package org.nodex.api.introduction.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.introduction.IntroductionRequest;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class IntroductionRequestReceivedEvent
		extends ConversationMessageReceivedEvent<IntroductionRequest> {
	public IntroductionRequestReceivedEvent(
			IntroductionRequest introductionRequest, ContactId contactId) {
		super(introductionRequest, contactId);
	}
}