package org.nodex.introduction;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class MessageMetadata {
	private final MessageType type;
	@Nullable
	private final SessionId sessionId;
	private final long timestamp, autoDeleteTimer;
	private final boolean local, read, visible, available, isAutoDecline;
	MessageMetadata(MessageType type, @Nullable SessionId sessionId,
			long timestamp, boolean local, boolean read, boolean visible,
			boolean available, long autoDeleteTimer, boolean isAutoDecline) {
		this.type = type;
		this.sessionId = sessionId;
		this.timestamp = timestamp;
		this.local = local;
		this.read = read;
		this.visible = visible;
		this.available = available;
		this.autoDeleteTimer = autoDeleteTimer;
		this.isAutoDecline = isAutoDecline;
	}
	MessageType getMessageType() {
		return type;
	}
	@Nullable
	public SessionId getSessionId() {
		return sessionId;
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
	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
	public boolean isAutoDecline() {
		return isAutoDecline;
	}
}
