package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class JoinMessage extends DeletableGroupInvitationMessage {
	@Nullable
	private final MessageId previousMessageId;
	JoinMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp, autoDeleteTimer);
		this.previousMessageId = previousMessageId;
	}
	@Nullable
	MessageId getPreviousMessageId() {
		return previousMessageId;
	}
}