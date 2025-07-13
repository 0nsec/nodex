package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.crypto.KeyPair;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.crypto.SecretKey;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.db.ContactExistsException;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.properties.TransportPropertyManager;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.api.transport.KeyManager;
import org.nodex.api.transport.KeySetId;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.introduction.IntroductionRequest;
import org.nodex.api.introduction.event.IntroductionAbortedEvent;
import org.nodex.api.introduction.event.IntroductionRequestReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static java.lang.Math.max;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.api.system.ClockConstants.MIN_REASONABLE_TIME_MS;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.introduction.IntroduceeState.AWAIT_AUTH;
import static org.nodex.introduction.IntroduceeState.AWAIT_RESPONSES;
import static org.nodex.introduction.IntroduceeState.LOCAL_ACCEPTED;
import static org.nodex.introduction.IntroduceeState.LOCAL_DECLINED;
import static org.nodex.introduction.IntroduceeState.REMOTE_ACCEPTED;
import static org.nodex.introduction.IntroduceeState.REMOTE_DECLINED;
import static org.nodex.introduction.IntroduceeState.START;
import static org.nodex.nullsafety.NullSafety.requireNonNull;
@Immutable
@NotNullByDefault
class IntroduceeProtocolEngine
		extends AbstractProtocolEngine<IntroduceeSession> {
	private final static Logger LOG =
			Logger.getLogger(IntroduceeProtocolEngine.class.getName());
	private final IntroductionCrypto crypto;
	private final KeyManager keyManager;
	private final TransportPropertyManager transportPropertyManager;
	@Inject
	IntroduceeProtocolEngine(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ContactManager contactManager,
			ContactGroupFactory contactGroupFactory,
			MessageTracker messageTracker,
			IdentityManager identityManager,
			AuthorManager authorManager,
			MessageParser messageParser,
			MessageEncoder messageEncoder,
			IntroductionCrypto crypto,
			KeyManager keyManager,
			TransportPropertyManager transportPropertyManager,
			ClientVersioningManager clientVersioningManager,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock) {
		super(db, clientHelper, contactManager, contactGroupFactory,
				messageTracker, identityManager, authorManager, messageParser,
				messageEncoder, clientVersioningManager, autoDeleteManager,
				conversationManager, clock);
		this.crypto = crypto;
		this.keyManager = keyManager;
		this.transportPropertyManager = transportPropertyManager;
	}
	@Override
	public IntroduceeSession onRequestAction(Transaction txn,
			IntroduceeSession session, @Nullable String text) {
		throw new UnsupportedOperationException();
	}
	@Override
	public IntroduceeSession onAcceptAction(Transaction txn,
			IntroduceeSession session) throws DbException {
		switch (session.getState()) {
			case AWAIT_RESPONSES:
			case REMOTE_DECLINED:
			case REMOTE_ACCEPTED:
				return onLocalAccept(txn, session);
			case START:
			case LOCAL_DECLINED:
			case LOCAL_ACCEPTED:
			case AWAIT_AUTH:
			case AWAIT_ACTIVATE:
				throw new ProtocolStateException();
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onDeclineAction(Transaction txn,
			IntroduceeSession session, boolean isAutoDecline)
			throws DbException {
		switch (session.getState()) {
			case AWAIT_RESPONSES:
			case REMOTE_DECLINED:
			case REMOTE_ACCEPTED:
				return onLocalDecline(txn, session, isAutoDecline);
			case START:
			case LOCAL_DECLINED:
			case LOCAL_ACCEPTED:
			case AWAIT_AUTH:
			case AWAIT_ACTIVATE:
				throw new ProtocolStateException();
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onRequestMessage(Transaction txn,
			IntroduceeSession session, RequestMessage m) throws DbException {
		switch (session.getState()) {
			case START:
				return onRemoteRequest(txn, session, m);
			case AWAIT_RESPONSES:
			case LOCAL_DECLINED:
			case REMOTE_DECLINED:
			case LOCAL_ACCEPTED:
			case REMOTE_ACCEPTED:
			case AWAIT_AUTH:
			case AWAIT_ACTIVATE:
				return abort(txn, session, m.getMessageId());
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onAcceptMessage(Transaction txn,
			IntroduceeSession session, AcceptMessage m) throws DbException {
		switch (session.getState()) {
			case LOCAL_DECLINED:
				return onRemoteResponseWhenDeclined(txn, session, m);
			case AWAIT_RESPONSES:
			case LOCAL_ACCEPTED:
				return onRemoteAccept(txn, session, m);
			case START:
			case REMOTE_DECLINED:
			case REMOTE_ACCEPTED:
			case AWAIT_AUTH:
			case AWAIT_ACTIVATE:
				return abort(txn, session, m.getMessageId());
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onDeclineMessage(Transaction txn,
			IntroduceeSession session, DeclineMessage m) throws DbException {
		switch (session.getState()) {
			case LOCAL_DECLINED:
				return onRemoteResponseWhenDeclined(txn, session, m);
			case AWAIT_RESPONSES:
			case LOCAL_ACCEPTED:
				return onRemoteDecline(txn, session, m);
			case START:
			case REMOTE_DECLINED:
			case REMOTE_ACCEPTED:
			case AWAIT_AUTH:
			case AWAIT_ACTIVATE:
				return abort(txn, session, m.getMessageId());
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onAuthMessage(Transaction txn,
			IntroduceeSession session, AuthMessage m) throws DbException {
		switch (session.getState()) {
			case AWAIT_AUTH:
				return onRemoteAuth(txn, session, m);
			case START:
			case AWAIT_RESPONSES:
			case LOCAL_DECLINED:
			case REMOTE_DECLINED:
			case LOCAL_ACCEPTED:
			case REMOTE_ACCEPTED:
			case AWAIT_ACTIVATE:
				return abort(txn, session, m.getMessageId());
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onActivateMessage(Transaction txn,
			IntroduceeSession session, ActivateMessage m) throws DbException {
		switch (session.getState()) {
			case AWAIT_ACTIVATE:
				return onRemoteActivate(txn, session, m);
			case START:
			case AWAIT_RESPONSES:
			case LOCAL_DECLINED:
			case REMOTE_DECLINED:
			case LOCAL_ACCEPTED:
			case REMOTE_ACCEPTED:
			case AWAIT_AUTH:
				return abort(txn, session, m.getMessageId());
			default:
				throw new AssertionError();
		}
	}
	@Override
	public IntroduceeSession onAbortMessage(Transaction txn,
			IntroduceeSession session, AbortMessage m) throws DbException {
		return onRemoteAbort(txn, session, m);
	}
	private IntroduceeSession onRemoteRequest(Transaction txn,
			IntroduceeSession s, RequestMessage m) throws DbException {
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		markMessageVisibleInUi(txn, m.getMessageId());
		markRequestAvailableToAnswer(txn, m.getMessageId(), true);
		addSessionId(txn, m.getMessageId(), s.getSessionId());
		conversationManager
				.trackMessage(txn, m.getGroupId(), m.getTimestamp(), false);
		receiveAutoDeleteTimer(txn, m);
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		Contact c = contactManager.getContact(txn, s.getIntroducer().getId(),
				localAuthor.getId());
		AuthorInfo authorInfo =
				authorManager.getAuthorInfo(txn, m.getAuthor().getId());
		IntroductionRequest request = new IntroductionRequest(m.getMessageId(),
				m.getGroupId(), m.getTimestamp(), false, false, false, false,
				s.getSessionId(), m.getAuthor(), m.getText(), false,
				authorInfo, m.getAutoDeleteTimer());
		IntroductionRequestReceivedEvent e =
				new IntroductionRequestReceivedEvent(request, c.getId());
		txn.attach(e);
		return IntroduceeSession.addRemoteRequest(s, AWAIT_RESPONSES, m);
	}
	private IntroduceeSession onLocalAccept(Transaction txn,
			IntroduceeSession s) throws DbException {
		markRequestsUnavailableToAnswer(txn, s);
		KeyPair keyPair = crypto.generateAgreementKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		Map<TransportId, TransportProperties> transportProperties =
				transportPropertyManager.getLocalProperties(txn);
		long localTimestamp = getTimestampForVisibleMessage(txn, s);
		Message sent = sendAcceptMessage(txn, s, localTimestamp, publicKey,
				localTimestamp, transportProperties, true);
		conversationManager.trackOutgoingMessage(txn, sent);
		switch (s.getState()) {
			case AWAIT_RESPONSES:
				return IntroduceeSession.addLocalAccept(s, LOCAL_ACCEPTED, sent,
						publicKey, privateKey, localTimestamp,
						transportProperties);
			case REMOTE_DECLINED:
				return IntroduceeSession.clear(s, START, sent.getId(),
						localTimestamp, s.getLastRemoteMessageId());
			case REMOTE_ACCEPTED:
				return onLocalAuth(txn, IntroduceeSession.addLocalAccept(s,
						AWAIT_AUTH, sent, publicKey, privateKey, localTimestamp,
						transportProperties));
			default:
				throw new AssertionError();
		}
	}
	private IntroduceeSession onLocalDecline(Transaction txn,
			IntroduceeSession s, boolean isAutoDecline) throws DbException {
		markRequestsUnavailableToAnswer(txn, s);
		long localTimestamp = getTimestampForVisibleMessage(txn, s);
		Message sent =
				sendDeclineMessage(txn, s, localTimestamp, true, isAutoDecline);
		conversationManager.trackOutgoingMessage(txn, sent);
		IntroduceeState state =
				s.getState() == AWAIT_RESPONSES ? LOCAL_DECLINED : START;
		return IntroduceeSession.clear(s, state, sent.getId(),
				sent.getTimestamp(), s.getLastRemoteMessageId());
	}
	private IntroduceeSession onRemoteAccept(Transaction txn,
			IntroduceeSession s, AcceptMessage m) throws DbException {
		if (m.getTimestamp() <= s.getRequestTimestamp())
			return abort(txn, s, m.getMessageId());
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		IntroduceeState state =
				s.getState() == AWAIT_RESPONSES ? REMOTE_ACCEPTED : AWAIT_AUTH;
		if (state == AWAIT_AUTH) {
			return onLocalAuth(txn,
					IntroduceeSession.addRemoteAccept(s, AWAIT_AUTH, m));
		}
		return IntroduceeSession.addRemoteAccept(s, state, m);
	}
	private IntroduceeSession onRemoteDecline(Transaction txn,
			IntroduceeSession s, DeclineMessage m) throws DbException {
		if (m.getTimestamp() <= s.getRequestTimestamp())
			return abort(txn, s, m.getMessageId());
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		markMessageVisibleInUi(txn, m.getMessageId());
		conversationManager
				.trackMessage(txn, m.getGroupId(), m.getTimestamp(), false);
		receiveAutoDeleteTimer(txn, m);
		broadcastIntroductionResponseReceivedEvent(txn, s,
				s.getIntroducer().getId(), s.getRemote().author, m, false);
		IntroduceeState state =
				s.getState() == AWAIT_RESPONSES ? REMOTE_DECLINED : START;
		return IntroduceeSession.clear(s, state, s.getLastLocalMessageId(),
				s.getLocalTimestamp(), m.getMessageId());
	}
	private IntroduceeSession onRemoteResponseWhenDeclined(Transaction txn,
			IntroduceeSession s, AbstractIntroductionMessage m)
			throws DbException {
		if (m.getTimestamp() <= s.getRequestTimestamp())
			return abort(txn, s, m.getMessageId());
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		return IntroduceeSession.clear(s, START, s.getLastLocalMessageId(),
				s.getLocalTimestamp(), m.getMessageId());
	}
	private IntroduceeSession onLocalAuth(Transaction txn, IntroduceeSession s)
			throws DbException {
		byte[] mac;
		byte[] signature;
		SecretKey masterKey, aliceMacKey, bobMacKey;
		try {
			masterKey = crypto.deriveMasterKey(s);
			aliceMacKey = crypto.deriveMacKey(masterKey, true);
			bobMacKey = crypto.deriveMacKey(masterKey, false);
			SecretKey ourMacKey = s.getLocal().alice ? aliceMacKey : bobMacKey;
			LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
			mac = crypto.authMac(ourMacKey, s, localAuthor.getId());
			signature = crypto.sign(ourMacKey, localAuthor.getPrivateKey());
		} catch (GeneralSecurityException e) {
			logException(LOG, WARNING, e);
			return abort(txn, s, s.getLastRemoteMessageId());
		}
		if (s.getState() != AWAIT_AUTH) throw new AssertionError();
		long localTimestamp = getTimestampForInvisibleMessage(s);
		Message sent = sendAuthMessage(txn, s, localTimestamp, mac, signature);
		return IntroduceeSession.addLocalAuth(s, AWAIT_AUTH, sent, masterKey,
				aliceMacKey, bobMacKey);
	}
	private IntroduceeSession onRemoteAuth(Transaction txn,
			IntroduceeSession s, AuthMessage m) throws DbException {
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		try {
			crypto.verifyAuthMac(m.getMac(), s, localAuthor.getId());
			crypto.verifySignature(m.getSignature(), s);
		} catch (GeneralSecurityException e) {
			return abort(txn, s, m.getMessageId());
		}
		long timestamp = Math.min(s.getLocal().acceptTimestamp,
				s.getRemote().acceptTimestamp);
		if (timestamp == -1) throw new AssertionError();
		if (timestamp < MIN_REASONABLE_TIME_MS) {
			LOG.warning("Timestamp is too old");
			return abort(txn, s, m.getMessageId());
		}
		Map<TransportId, KeySetId> keys = null;
		try {
			ContactId contactId = contactManager.addContact(txn,
					s.getRemote().author, localAuthor.getId(), false);
			keys = keyManager.addRotationKeys(txn, contactId,
					new SecretKey(s.getMasterKey()), timestamp,
					s.getLocal().alice, false);
			transportPropertyManager.addRemoteProperties(txn, contactId,
					requireNonNull(s.getRemote().transportProperties));
		} catch (ContactExistsException e) {
		}
		byte[] mac = crypto.activateMac(s);
		long localTimestamp = getTimestampForInvisibleMessage(s);
		Message sent = sendActivateMessage(txn, s, localTimestamp, mac);
		return IntroduceeSession.awaitActivate(s, m, sent, keys);
	}
	private IntroduceeSession onRemoteActivate(Transaction txn,
			IntroduceeSession s, ActivateMessage m) throws DbException {
		if (isInvalidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s, m.getMessageId());
		try {
			crypto.verifyActivateMac(m.getMac(), s);
		} catch (GeneralSecurityException e) {
			return abort(txn, s, m.getMessageId());
		}
		if (s.getTransportKeys() != null) {
			keyManager.activateKeys(txn, s.getTransportKeys());
		}
		return IntroduceeSession.clear(s, START, s.getLastLocalMessageId(),
				s.getLocalTimestamp(), m.getMessageId());
	}
	private IntroduceeSession onRemoteAbort(Transaction txn,
			IntroduceeSession s, AbortMessage m)
			throws DbException {
		markRequestsUnavailableToAnswer(txn, s);
		txn.attach(new IntroductionAbortedEvent(s.getSessionId()));
		return IntroduceeSession.clear(s, START, s.getLastLocalMessageId(),
				s.getLocalTimestamp(), m.getMessageId());
	}
	private IntroduceeSession abort(Transaction txn, IntroduceeSession s,
			@Nullable MessageId lastRemoteMessageId) throws DbException {
		markRequestsUnavailableToAnswer(txn, s);
		long localTimestamp = getTimestampForInvisibleMessage(s);
		Message sent = sendAbortMessage(txn, s, localTimestamp);
		txn.attach(new IntroductionAbortedEvent(s.getSessionId()));
		return IntroduceeSession.clear(s, START, sent.getId(),
				sent.getTimestamp(), lastRemoteMessageId);
	}
	private boolean isInvalidDependency(IntroduceeSession s,
			@Nullable MessageId dependency) {
		return isInvalidDependency(s.getLastRemoteMessageId(), dependency);
	}
	private long getTimestampForVisibleMessage(Transaction txn,
			IntroduceeSession s) throws DbException {
		long conversationTimestamp =
				getTimestampForOutgoingMessage(txn, s.getContactGroupId());
		return max(conversationTimestamp, getSessionTimestamp(s) + 1);
	}
	private long getTimestampForInvisibleMessage(IntroduceeSession s) {
		return max(clock.currentTimeMillis(), getSessionTimestamp(s) + 1);
	}
	private long getSessionTimestamp(IntroduceeSession s) {
		return max(s.getLocalTimestamp(), s.getRequestTimestamp());
	}
	private void addSessionId(Transaction txn, MessageId m, SessionId sessionId)
			throws DbException {
		BdfDictionary meta = new BdfDictionary();
		messageEncoder.addSessionId(meta, sessionId);
		try {
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	private void markRequestsUnavailableToAnswer(Transaction txn,
			IntroduceeSession s) throws DbException {
		BdfDictionary query = messageParser
				.getRequestsAvailableToAnswerQuery(s.getSessionId());
		try {
			Collection<MessageId> results = clientHelper.getMessageIds(txn,
					s.getContactGroupId(), query);
			for (MessageId m : results)
				markRequestAvailableToAnswer(txn, m, false);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	private void markRequestAvailableToAnswer(Transaction txn, MessageId m,
			boolean available) throws DbException {
		BdfDictionary meta = new BdfDictionary();
		messageEncoder.setAvailableToAnswer(meta, available);
		try {
			clientHelper.mergeMessageMetadata(txn, m, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
}
