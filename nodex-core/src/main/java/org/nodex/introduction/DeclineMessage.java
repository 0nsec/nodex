package org.nodex.introduction;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class DeclineMessage extends AbstractIntroductionMessage {
	private final SessionId sessionId;
	protected DeclineMessage(MessageId messageId, GroupId groupId,
			long timestamp, @Nullable MessageId previousMessageId,
			SessionId sessionId, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.sessionId = sessionId;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
}