package org.nodex.api.blog.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.blog.Blog;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class BlogInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Blog>> {
	public BlogInvitationRequestReceivedEvent(ConversationRequest<Blog> request,
			ContactId contactId) {
		super(request, contactId);
	}
}