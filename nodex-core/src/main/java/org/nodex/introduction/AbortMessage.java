package org.nodex.introduction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
@Immutable
@NotNullByDefault
class AbortMessage extends AbstractIntroductionMessage {
	private final SessionId sessionId;
	protected AbortMessage(MessageId messageId, GroupId groupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId) {
		super(messageId, groupId, timestamp, previousMessageId,
				NO_AUTO_DELETE_TIMER);
		this.sessionId = sessionId;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
}
