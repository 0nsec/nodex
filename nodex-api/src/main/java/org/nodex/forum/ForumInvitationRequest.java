package org.nodex.api.forum;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.sharing.InvitationRequest;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumInvitationRequest extends InvitationRequest<Forum> {
	public ForumInvitationRequest(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, Forum forum, @Nullable String text,
			boolean available, boolean canBeOpened, long autoDeleteTimer) {
		super(id, groupId, time, local, read, sent, seen, sessionId, forum,
				text, available, canBeOpened, autoDeleteTimer);
	}
	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitForumInvitationRequest(this);
	}
}