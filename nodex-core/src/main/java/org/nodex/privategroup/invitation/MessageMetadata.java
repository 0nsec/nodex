package org.nodex.privategroup.invitation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class MessageMetadata {
	private final MessageType type;
	private final GroupId privateGroupId;
	private final long timestamp, autoDeleteTimer;
	private final boolean local, read, visible, available, accepted;
	private final boolean isAutoDecline;
	MessageMetadata(MessageType type, GroupId privateGroupId,
			long timestamp, boolean local, boolean read, boolean visible,
			boolean available, boolean accepted, long autoDeleteTimer,
			boolean isAutoDecline) {
		this.privateGroupId = privateGroupId;
		this.type = type;
		this.timestamp = timestamp;
		this.local = local;
		this.read = read;
		this.visible = visible;
		this.available = available;
		this.accepted = accepted;
		this.autoDeleteTimer = autoDeleteTimer;
		this.isAutoDecline = isAutoDecline;
	}
	MessageType getMessageType() {
		return type;
	}
	GroupId getPrivateGroupId() {
		return privateGroupId;
	}
	long getTimestamp() {
		return timestamp;
	}
	boolean isLocal() {
		return local;
	}
	boolean isRead() {
		return read;
	}
	boolean isVisibleInConversation() {
		return visible;
	}
	boolean isAvailableToAnswer() {
		return available;
	}
	public boolean wasAccepted() {
		return accepted;
	}
	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
	public boolean isAutoDecline() {
		return isAutoDecline;
	}
}
