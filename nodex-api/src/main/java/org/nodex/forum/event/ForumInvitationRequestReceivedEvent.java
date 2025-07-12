package org.nodex.api.forum.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.forum.Forum;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Forum>> {
	public ForumInvitationRequestReceivedEvent(ConversationRequest<Forum> request,
			ContactId contactId) {
		super(request, contactId);
	}
}