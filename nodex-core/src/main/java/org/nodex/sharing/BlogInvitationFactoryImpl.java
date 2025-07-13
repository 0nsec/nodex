package org.nodex.sharing;
import org.nodex.api.contact.ContactId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogInvitationRequest;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.client.SessionId;
import javax.inject.Inject;
public class BlogInvitationFactoryImpl
		implements InvitationFactory<Blog, BlogInvitationResponse> {
	@Inject
	BlogInvitationFactoryImpl() {
	}
	@Override
	public BlogInvitationRequest createInvitationRequest(boolean local,
			boolean sent, boolean seen, boolean read, InviteMessage<Blog> m,
			ContactId c, boolean available, boolean canBeOpened,
			long autoDeleteTimer) {
		SessionId sessionId = new SessionId(m.getShareableId().getBytes());
		return new BlogInvitationRequest(m.getId(), m.getContactGroupId(),
				m.getTimestamp(), local, read, sent, seen, sessionId,
				m.getShareable(), m.getText(), available, canBeOpened,
				autoDeleteTimer);
	}
	@Override
	public BlogInvitationResponse createInvitationResponse(MessageId id,
			GroupId contactGroupId, long time, boolean local, boolean sent,
			boolean seen, boolean read, boolean accept, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		SessionId sessionId = new SessionId(shareableId.getBytes());
		return new BlogInvitationResponse(id, contactGroupId, time, local, read,
				sent, seen, sessionId, accept, shareableId,
				autoDeleteTimer, isAutoDecline);
	}
}
