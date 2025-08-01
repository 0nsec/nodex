package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
abstract class GroupInvitationMessage {
	private final MessageId id;
	private final GroupId contactGroupId, privateGroupId;
	private final long timestamp;
	GroupInvitationMessage(MessageId id, GroupId contactGroupId,
			GroupId privateGroupId, long timestamp) {
		this.id = id;
		this.contactGroupId = contactGroupId;
		this.privateGroupId = privateGroupId;
		this.timestamp = timestamp;
	}
	MessageId getId() {
		return id;
	}
	GroupId getContactGroupId() {
		return contactGroupId;
	}
	GroupId getPrivateGroupId() {
		return privateGroupId;
	}
	long getTimestamp() {
		return timestamp;
	}
}
