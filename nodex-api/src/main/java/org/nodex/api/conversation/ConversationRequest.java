package org.nodex.api.conversation;
import org.nodex.api.Nameable;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ConversationRequest<N extends Nameable>
		extends ConversationMessageHeader {
	private final SessionId sessionId;
	private final N nameable;
	@Nullable
	private final String text;
	private final boolean answered;
	public ConversationRequest(MessageId messageId, GroupId groupId,
			long timestamp, boolean local, boolean read, boolean sent,
			boolean seen, SessionId sessionId, N nameable,
			@Nullable String text, boolean answered, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, local, read, sent, seen,
				autoDeleteTimer);
		this.sessionId = sessionId;
		this.nameable = nameable;
		this.text = text;
		this.answered = answered;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
	public N getNameable() {
		return nameable;
	}
	public String getName() {
		return nameable.getName();
	}
	@Nullable
	public String getText() {
		return text;
	}
	public boolean wasAnswered() {
		return answered;
	}
}