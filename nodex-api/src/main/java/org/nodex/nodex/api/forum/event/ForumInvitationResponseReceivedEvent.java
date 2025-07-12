package org.nodex.api.forum.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumInvitationResponseReceivedEvent extends
		ConversationMessageReceivedEvent<ForumInvitationResponse> {
	public ForumInvitationResponseReceivedEvent(
			ForumInvitationResponse response, ContactId contactId) {
		super(response, contactId);
	}
}