package org.nodex.api.privategroup.invitation;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.sharing.InvitationRequest;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupInvitationRequest extends InvitationRequest<PrivateGroup> {
	public GroupInvitationRequest(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, PrivateGroup shareable,
			@Nullable String text, boolean available, boolean canBeOpened,
			long autoDeleteTimer) {
		super(id, groupId, time, local, read, sent, seen, sessionId, shareable,
				text, available, canBeOpened, autoDeleteTimer);
	}
	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitGroupInvitationRequest(this);
	}
}