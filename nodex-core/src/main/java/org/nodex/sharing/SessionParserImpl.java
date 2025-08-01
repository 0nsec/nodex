package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_INVITE_TIMESTAMP;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_IS_SESSION;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_LAST_LOCAL_MESSAGE_ID;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_LAST_REMOTE_MESSAGE_ID;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_LOCAL_TIMESTAMP;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_SESSION_ID;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_SHAREABLE_ID;
import static org.nodex.sharing.SharingConstants.SESSION_KEY_STATE;
@Immutable
@NotNullByDefault
class SessionParserImpl implements SessionParser {
	@Inject
	SessionParserImpl() {
	}
	@Override
	public BdfDictionary getSessionQuery(SessionId s) {
		return BdfDictionary.of(BdfEntry.of(SESSION_KEY_SESSION_ID, s));
	}
	@Override
	public BdfDictionary getAllSessionsQuery() {
		return BdfDictionary.of(BdfEntry.of(SESSION_KEY_IS_SESSION, true));
	}
	@Override
	public boolean isSession(BdfDictionary d) throws FormatException {
		return d.getBoolean(SESSION_KEY_IS_SESSION, false);
	}
	@Override
	public Session parseSession(GroupId contactGroupId,
			BdfDictionary d) throws FormatException {
		return new Session(State.fromValue(getState(d)), contactGroupId,
				getShareableId(d), getLastLocalMessageId(d),
				getLastRemoteMessageId(d), getLocalTimestamp(d),
				getInviteTimestamp(d));
	}
	private int getState(BdfDictionary d) throws FormatException {
		return d.getInt(SESSION_KEY_STATE);
	}
	private GroupId getShareableId(BdfDictionary d) throws FormatException {
		return new GroupId(d.getRaw(SESSION_KEY_SHAREABLE_ID));
	}
	@Nullable
	private MessageId getLastLocalMessageId(BdfDictionary d)
			throws FormatException {
		byte[] b = d.getOptionalRaw(SESSION_KEY_LAST_LOCAL_MESSAGE_ID);
		return b == null ? null : new MessageId(b);
	}
	@Nullable
	private MessageId getLastRemoteMessageId(BdfDictionary d)
			throws FormatException {
		byte[] b = d.getOptionalRaw(SESSION_KEY_LAST_REMOTE_MESSAGE_ID);
		return b == null ? null : new MessageId(b);
	}
	private long getLocalTimestamp(BdfDictionary d) throws FormatException {
		return d.getLong(SESSION_KEY_LOCAL_TIMESTAMP);
	}
	private long getInviteTimestamp(BdfDictionary d) throws FormatException {
		return d.getLong(SESSION_KEY_INVITE_TIMESTAMP);
	}
}
