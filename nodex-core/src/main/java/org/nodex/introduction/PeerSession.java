package org.nodex.introduction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
interface PeerSession {
	SessionId getSessionId();
	GroupId getContactGroupId();
	long getLocalTimestamp();
	@Nullable
	MessageId getLastLocalMessageId();
	@Nullable
	MessageId getLastRemoteMessageId();
}