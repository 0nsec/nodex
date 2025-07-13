package org.nodex.introduction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
@Immutable
@NotNullByDefault
class ActivateMessage extends AbstractIntroductionMessage {
	private final SessionId sessionId;
	private final byte[] mac;
	protected ActivateMessage(MessageId messageId, GroupId groupId,
			long timestamp, MessageId previousMessageId, SessionId sessionId,
			byte[] mac) {
		super(messageId, groupId, timestamp, previousMessageId,
				NO_AUTO_DELETE_TIMER);
		this.sessionId = sessionId;
		this.mac = mac;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
	public byte[] getMac() {
		return mac;
	}
}
