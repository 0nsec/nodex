package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.crypto.AgreementPrivateKey;
import org.nodex.api.crypto.AgreementPublicKey;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.identity.Author;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.transport.KeySetId;
import org.nodex.api.client.SessionId;
import org.nodex.api.introduction.Role;
import org.nodex.introduction.IntroduceeSession.Local;
import org.nodex.introduction.IntroduceeSession.Remote;
import org.nodex.introduction.IntroducerSession.Introducee;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.introduction.Role.INTRODUCEE;
import static org.nodex.api.introduction.Role.INTRODUCER;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_ACCEPT_TIMESTAMP;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_ALICE;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_AUTHOR;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_EPHEMERAL_PRIVATE_KEY;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_EPHEMERAL_PUBLIC_KEY;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_GROUP_ID;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_INTRODUCEE_A;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_INTRODUCEE_B;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_INTRODUCER;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_LAST_LOCAL_MESSAGE_ID;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_LAST_REMOTE_MESSAGE_ID;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_LOCAL;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_LOCAL_TIMESTAMP;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_MAC_KEY;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_MASTER_KEY;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_REMOTE;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_REMOTE_AUTHOR;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_REQUEST_TIMESTAMP;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_ROLE;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_SESSION_ID;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_STATE;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_TRANSPORT_KEYS;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_TRANSPORT_PROPERTIES;
@Immutable
@NotNullByDefault
class SessionParserImpl implements SessionParser {
	private final ClientHelper clientHelper;
	@Inject
	SessionParserImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}
	@Override
	public BdfDictionary getSessionQuery(SessionId s) {
		return BdfDictionary.of(BdfEntry.of(SESSION_KEY_SESSION_ID, s));
	}
	@Override
	public Role getRole(BdfDictionary d) throws FormatException {
		return Role.fromValue(d.getInt(SESSION_KEY_ROLE));
	}
	@Override
	public IntroducerSession parseIntroducerSession(BdfDictionary d)
			throws FormatException {
		if (getRole(d) != INTRODUCER) throw new IllegalArgumentException();
		SessionId sessionId = getSessionId(d);
		IntroducerState state = IntroducerState.fromValue(getState(d));
		long requestTimestamp = d.getLong(SESSION_KEY_REQUEST_TIMESTAMP);
		Introducee introduceeA = parseIntroducee(sessionId,
				d.getDictionary(SESSION_KEY_INTRODUCEE_A));
		Introducee introduceeB = parseIntroducee(sessionId,
				d.getDictionary(SESSION_KEY_INTRODUCEE_B));
		return new IntroducerSession(sessionId, state, requestTimestamp,
				introduceeA, introduceeB);
	}
	private Introducee parseIntroducee(SessionId sessionId, BdfDictionary d)
			throws FormatException {
		MessageId lastLocalMessageId =
				getMessageId(d, SESSION_KEY_LAST_LOCAL_MESSAGE_ID);
		MessageId lastRemoteMessageId =
				getMessageId(d, SESSION_KEY_LAST_REMOTE_MESSAGE_ID);
		long localTimestamp = d.getLong(SESSION_KEY_LOCAL_TIMESTAMP);
		GroupId groupId = getGroupId(d);
		Author author = getAuthor(d, SESSION_KEY_AUTHOR);
		return new Introducee(sessionId, groupId, author, localTimestamp,
				lastLocalMessageId, lastRemoteMessageId);
	}
	@Override
	public IntroduceeSession parseIntroduceeSession(GroupId introducerGroupId,
			BdfDictionary d) throws FormatException {
		if (getRole(d) != INTRODUCEE) throw new IllegalArgumentException();
		SessionId sessionId = getSessionId(d);
		IntroduceeState state = IntroduceeState.fromValue(getState(d));
		long requestTimestamp = d.getLong(SESSION_KEY_REQUEST_TIMESTAMP);
		Author introducer = getAuthor(d, SESSION_KEY_INTRODUCER);
		Local local = parseLocal(d.getDictionary(SESSION_KEY_LOCAL));
		Remote remote = parseRemote(d.getDictionary(SESSION_KEY_REMOTE));
		byte[] masterKey = d.getOptionalRaw(SESSION_KEY_MASTER_KEY);
		Map<TransportId, KeySetId> transportKeys = parseTransportKeys(
				d.getOptionalDictionary(SESSION_KEY_TRANSPORT_KEYS));
		return new IntroduceeSession(sessionId, state, requestTimestamp,
				introducerGroupId, introducer, local, remote,
				masterKey, transportKeys);
	}
	private Local parseLocal(BdfDictionary d) throws FormatException {
		boolean alice = d.getBoolean(SESSION_KEY_ALICE);
		MessageId lastLocalMessageId =
				getMessageId(d, SESSION_KEY_LAST_LOCAL_MESSAGE_ID);
		long localTimestamp = d.getLong(SESSION_KEY_LOCAL_TIMESTAMP);
		PublicKey ephemeralPublicKey = getEphemeralPublicKey(d);
		BdfDictionary tpDict =
				d.getOptionalDictionary(SESSION_KEY_TRANSPORT_PROPERTIES);
		PrivateKey ephemeralPrivateKey = getEphemeralPrivateKey(d);
		Map<TransportId, TransportProperties> transportProperties =
				tpDict == null ? null : clientHelper
						.parseAndValidateTransportPropertiesMap(tpDict);
		long acceptTimestamp = d.getLong(SESSION_KEY_ACCEPT_TIMESTAMP);
		byte[] macKey = d.getOptionalRaw(SESSION_KEY_MAC_KEY);
		return new Local(alice, lastLocalMessageId, localTimestamp,
				ephemeralPublicKey, ephemeralPrivateKey, transportProperties,
				acceptTimestamp, macKey);
	}
	private Remote parseRemote(BdfDictionary d) throws FormatException {
		boolean alice = d.getBoolean(SESSION_KEY_ALICE);
		Author remoteAuthor = getAuthor(d, SESSION_KEY_REMOTE_AUTHOR);
		MessageId lastRemoteMessageId =
				getMessageId(d, SESSION_KEY_LAST_REMOTE_MESSAGE_ID);
		PublicKey ephemeralPublicKey = getEphemeralPublicKey(d);
		BdfDictionary tpDict =
				d.getOptionalDictionary(SESSION_KEY_TRANSPORT_PROPERTIES);
		Map<TransportId, TransportProperties> transportProperties =
				tpDict == null ? null : clientHelper
						.parseAndValidateTransportPropertiesMap(tpDict);
		long acceptTimestamp = d.getLong(SESSION_KEY_ACCEPT_TIMESTAMP);
		byte[] macKey = d.getOptionalRaw(SESSION_KEY_MAC_KEY);
		return new Remote(alice, remoteAuthor, lastRemoteMessageId,
				ephemeralPublicKey, transportProperties, acceptTimestamp,
				macKey);
	}
	private int getState(BdfDictionary d) throws FormatException {
		return d.getInt(SESSION_KEY_STATE);
	}
	private SessionId getSessionId(BdfDictionary d) throws FormatException {
		byte[] b = d.getRaw(SESSION_KEY_SESSION_ID);
		return new SessionId(b);
	}
	@Nullable
	private MessageId getMessageId(BdfDictionary d, String key)
			throws FormatException {
		byte[] b = d.getOptionalRaw(key);
		return b == null ? null : new MessageId(b);
	}
	private GroupId getGroupId(BdfDictionary d) throws FormatException {
		return new GroupId(d.getRaw(SESSION_KEY_GROUP_ID));
	}
	private Author getAuthor(BdfDictionary d, String key)
			throws FormatException {
		return clientHelper.parseAndValidateAuthor(d.getList(key));
	}
	@Nullable
	private Map<TransportId, KeySetId> parseTransportKeys(
			@Nullable BdfDictionary d) throws FormatException {
		if (d == null) return null;
		Map<TransportId, KeySetId> map = new HashMap<>(d.size());
		for (String key : d.keySet()) {
			map.put(new TransportId(key), new KeySetId(d.getInt(key)));
		}
		return map;
	}
	@Nullable
	private PublicKey getEphemeralPublicKey(BdfDictionary d)
			throws FormatException {
		byte[] keyBytes = d.getOptionalRaw(SESSION_KEY_EPHEMERAL_PUBLIC_KEY);
		return keyBytes == null ? null : new AgreementPublicKey(keyBytes);
	}
	@Nullable
	private PrivateKey getEphemeralPrivateKey(BdfDictionary d)
			throws FormatException {
		byte[] keyBytes = d.getOptionalRaw(SESSION_KEY_EPHEMERAL_PRIVATE_KEY);
		return keyBytes == null ? null : new AgreementPrivateKey(keyBytes);
	}
}
