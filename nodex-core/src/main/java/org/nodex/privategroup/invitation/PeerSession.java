package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static org.nodex.privategroup.invitation.PeerState.START;
import static org.nodex.privategroup.invitation.Role.PEER;
@Immutable
@NotNullByDefault
class PeerSession extends Session<PeerState> {
	private final PeerState state;
	PeerSession(GroupId contactGroupId, GroupId privateGroupId,
			@Nullable MessageId lastLocalMessageId,
			@Nullable MessageId lastRemoteMessageId, long localTimestamp,
			PeerState state) {
		super(contactGroupId, privateGroupId, lastLocalMessageId,
				lastRemoteMessageId, localTimestamp, 0);
		this.state = state;
	}
	PeerSession(GroupId contactGroupId, GroupId privateGroupId) {
		this(contactGroupId, privateGroupId, null, null, 0, START);
	}
	@Override
	Role getRole() {
		return PEER;
	}
	@Override
	PeerState getState() {
		return state;
	}
}
