package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class AbortMessage extends GroupInvitationMessage {
	AbortMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp) {
		super(id, contactGroupId, privateGroupId, timestamp);
	}
}