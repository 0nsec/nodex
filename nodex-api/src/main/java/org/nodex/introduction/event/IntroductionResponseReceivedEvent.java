package org.nodex.api.introduction.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.introduction.IntroductionResponse;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class IntroductionResponseReceivedEvent extends
		ConversationMessageReceivedEvent<IntroductionResponse> {
	public IntroductionResponseReceivedEvent(
			IntroductionResponse introductionResponse, ContactId contactId) {
		super(introductionResponse, contactId);
	}
}