package org.nodex.api.blog.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class BlogInvitationResponseReceivedEvent
		extends ConversationMessageReceivedEvent<BlogInvitationResponse> {
	public BlogInvitationResponseReceivedEvent(BlogInvitationResponse response,
			ContactId contactId) {
		super(response, contactId);
	}
}