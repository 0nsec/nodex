package org.nodex.introduction;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.identity.Author;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Map;
import javax.annotation.Nullable;
@NotNullByDefault
interface MessageEncoder {
	BdfDictionary encodeRequestMetadata(long timestamp,
			long autoDeleteTimer);
	BdfDictionary encodeMetadata(MessageType type,
			@Nullable SessionId sessionId, long timestamp,
			long autoDeleteTimer);
	BdfDictionary encodeMetadata(MessageType type,
			@Nullable SessionId sessionId, long timestamp, boolean local,
			boolean read, boolean visible, long autoDeleteTimer,
			boolean isAutoDecline);
	void addSessionId(BdfDictionary meta, SessionId sessionId);
	void setVisibleInUi(BdfDictionary meta, boolean visible);
	void setAvailableToAnswer(BdfDictionary meta, boolean available);
	Message encodeRequestMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, Author author,
			@Nullable String text);
	Message encodeRequestMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, Author author,
			@Nullable String text, long autoDeleteTimer);
	Message encodeAcceptMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId,
			PublicKey ephemeralPublicKey, long acceptTimestamp,
			Map<TransportId, TransportProperties> transportProperties);
	Message encodeAcceptMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId,
			PublicKey ephemeralPublicKey, long acceptTimestamp,
			Map<TransportId, TransportProperties> transportProperties,
			long autoDeleteTimer);
	Message encodeDeclineMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId);
	Message encodeDeclineMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId,
			long autoDeleteTimer);
	Message encodeAuthMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId,
			byte[] mac, byte[] signature);
	Message encodeActivateMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId,
			byte[] mac);
	Message encodeAbortMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, SessionId sessionId);
}
