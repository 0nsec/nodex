package org.nodex.api.introduction.event;
import org.nodex.api.event.Event;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class IntroductionAbortedEvent extends Event {
	private final SessionId sessionId;
	public IntroductionAbortedEvent(SessionId sessionId) {
		this.sessionId = sessionId;
	}
	public SessionId getSessionId() {
		return sessionId;
	}
}