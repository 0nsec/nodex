package org.nodex.api.blog;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.sharing.InvitationResponse;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public class BlogInvitationResponse extends InvitationResponse {
	public BlogInvitationResponse(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accept, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(id, groupId, time, local, read, sent, seen, sessionId,
				accept, shareableId, autoDeleteTimer, isAutoDecline);
	}
	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitBlogInvitationResponse(this);
	}
}