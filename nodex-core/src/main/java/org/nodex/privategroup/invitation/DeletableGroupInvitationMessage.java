package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
abstract class DeletableGroupInvitationMessage extends GroupInvitationMessage {
	private final long autoDeleteTimer;
	DeletableGroupInvitationMessage(MessageId id, GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, long autoDeleteTimer) {
		super(id, contactGroupId, privateGroupId, timestamp);
		this.autoDeleteTimer = autoDeleteTimer;
	}
	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}
