package org.nodex.api.conversation;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ConversationResponse extends ConversationMessageHeader {
	private final SessionId sessionId;
	private final boolean accepted, isAutoDecline;
	public ConversationResponse(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accepted, long autoDeleteTimer,
			boolean isAutoDecline) {
		super(id, groupId, time, local, read, sent, seen, autoDeleteTimer);
		this.sessionId = sessionId;
		this.accepted = accepted;
		this.isAutoDecline = isAutoDecline;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
	public boolean wasAccepted() {
		return accepted;
	}
	public boolean isAutoDecline() {
		return isAutoDecline;
	}
}