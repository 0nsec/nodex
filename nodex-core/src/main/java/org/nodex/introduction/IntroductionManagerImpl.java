package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.cleanup.CleanupHook;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.contact.ContactManager.ContactHook;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Metadata;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Group.Visibility;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.MessageStatus;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.versioning.ClientVersioningManager.ClientVersioningHook;
import org.nodex.api.autodelete.event.ConversationMessagesDeletedEvent;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.DeletionResult;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.introduction.IntroductionManager;
import org.nodex.api.introduction.IntroductionRequest;
import org.nodex.api.introduction.IntroductionResponse;
import org.nodex.api.introduction.Role;
import org.nodex.client.ConversationClientImpl;
import org.nodex.introduction.IntroducerSession.Introducee;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.sync.validation.IncomingMessageHookConstants.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.introduction.Role.INTRODUCEE;
import static org.nodex.api.introduction.Role.INTRODUCER;
import static org.nodex.introduction.IntroduceeState.AWAIT_RESPONSES;
import static org.nodex.introduction.IntroduceeState.REMOTE_ACCEPTED;
import static org.nodex.introduction.IntroduceeState.REMOTE_DECLINED;
import static org.nodex.introduction.IntroducerState.A_DECLINED;
import static org.nodex.introduction.IntroducerState.B_DECLINED;
import static org.nodex.introduction.IntroducerState.START;
import static org.nodex.introduction.MessageType.ABORT;
import static org.nodex.introduction.MessageType.ACCEPT;
import static org.nodex.introduction.MessageType.ACTIVATE;
import static org.nodex.introduction.MessageType.AUTH;
import static org.nodex.introduction.MessageType.DECLINE;
import static org.nodex.introduction.MessageType.REQUEST;
@Immutable
@NotNullByDefault
class IntroductionManagerImpl extends ConversationClientImpl
		implements IntroductionManager, OpenDatabaseHook, ContactHook,
		ClientVersioningHook, CleanupHook {
	private final ClientVersioningManager clientVersioningManager;
	private final ContactGroupFactory contactGroupFactory;
	private final ContactManager contactManager;
	private final MessageParser messageParser;
	private final SessionEncoder sessionEncoder;
	private final SessionParser sessionParser;
	private final IntroducerProtocolEngine introducerEngine;
	private final IntroduceeProtocolEngine introduceeEngine;
	private final IntroductionCrypto crypto;
	private final IdentityManager identityManager;
	private final AuthorManager authorManager;
	private final Group localGroup;
	@Inject
	IntroductionManagerImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ContactManager contactManager,
			MessageParser messageParser,
			SessionEncoder sessionEncoder,
			SessionParser sessionParser,
			IntroducerProtocolEngine introducerEngine,
			IntroduceeProtocolEngine introduceeEngine,
			IntroductionCrypto crypto,
			IdentityManager identityManager,
			AuthorManager authorManager) {
		super(db, clientHelper, metadataParser, messageTracker);
		this.clientVersioningManager = clientVersioningManager;
		this.contactGroupFactory = contactGroupFactory;
		this.contactManager = contactManager;
		this.messageParser = messageParser;
		this.sessionEncoder = sessionEncoder;
		this.sessionParser = sessionParser;
		this.introducerEngine = introducerEngine;
		this.introduceeEngine = introduceeEngine;
		this.crypto = crypto;
		this.identityManager = identityManager;
		this.authorManager = authorManager;
		this.localGroup =
				contactGroupFactory.createLocalGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	}
	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		if (db.containsGroup(txn, localGroup.getId())) return;
		db.addGroup(txn, localGroup);
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}
	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		Group g = getContactGroup(c);
		db.addGroup(txn, g);
		Visibility client = clientVersioningManager.getClientVisibility(txn,
				c.getId(), CLIENT_ID.toString(), MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), g.getId(), client);
		clientHelper.setContactId(txn, g.getId(), c.getId());
	}
	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		removeSessionWithIntroducer(txn, c);
		abortOrRemoveSessionWithIntroducee(txn, c);
		db.removeGroup(txn, getContactGroup(c));
	}
	@Override
	public void onClientVisibilityChanging(Transaction txn, Contact c,
			Visibility v) throws DbException {
		Group g = getContactGroup(c);
		db.setGroupVisibility(txn, c.getId(), g.getId(), v);
	}
	@Override
	public Group getContactGroup(Contact c) {
		return contactGroupFactory
				.createContactGroup(CLIENT_ID.toString(), MAJOR_VERSION, c);
	}
	@Override
	protected DeliveryAction incomingMessage(Transaction txn, Message m,
			BdfList body, BdfDictionary bdfMeta)
			throws DbException, FormatException {
		MessageMetadata meta = messageParser.parseMetadata(bdfMeta);
		long timer = meta.getAutoDeleteTimer();
		if (timer != NO_AUTO_DELETE_TIMER) {
			db.setCleanupTimerDuration(txn, m.getId(), timer);
		}
		SessionId sessionId = meta.getSessionId();
		IntroduceeSession newIntroduceeSession = null;
		if (sessionId == null) {
			if (meta.getMessageType() != REQUEST) throw new AssertionError();
			newIntroduceeSession = createNewIntroduceeSession(txn, m, body);
			sessionId = newIntroduceeSession.getSessionId();
		}
		StoredSession ss = getSession(txn, sessionId);
		Session<?> session;
		MessageId storageId;
		if (ss == null) {
			if (meta.getMessageType() != REQUEST) throw new FormatException();
			if (newIntroduceeSession == null) throw new AssertionError();
			storageId = createStorageId(txn);
			session = handleMessage(txn, m, body, meta.getMessageType(),
					newIntroduceeSession, introduceeEngine);
		} else {
			storageId = ss.storageId;
			Role role = sessionParser.getRole(ss.bdfSession);
			if (role == INTRODUCER) {
				session = handleMessage(txn, m, body, meta.getMessageType(),
						sessionParser.parseIntroducerSession(ss.bdfSession),
						introducerEngine);
			} else if (role == INTRODUCEE) {
				session = handleMessage(txn, m, body, meta.getMessageType(),
						sessionParser.parseIntroduceeSession(m.getGroupId(),
								ss.bdfSession), introduceeEngine);
			} else throw new AssertionError();
		}
		storeSession(txn, storageId, session);
		return ACCEPT_DO_NOT_SHARE;
	}
	private IntroduceeSession createNewIntroduceeSession(Transaction txn,
			Message m, BdfList body) throws DbException, FormatException {
		ContactId introducerId = clientHelper.getContactId(txn, m.getGroupId());
		Author introducer = db.getContact(txn, introducerId).getAuthor();
		Author local = identityManager.getLocalAuthor(txn);
		Author remote = messageParser.parseRequestMessage(m, body).getAuthor();
		if (local.equals(remote)) throw new FormatException();
		SessionId sessionId = crypto.getSessionId(introducer, local, remote);
		boolean alice = crypto.isAlice(local.getId(), remote.getId());
		return IntroduceeSession
				.getInitial(m.getGroupId(), sessionId, introducer, alice,
						remote);
	}
	private <S extends Session<?>> S handleMessage(Transaction txn, Message m,
			BdfList body, MessageType type, S session, ProtocolEngine<S> engine)
			throws DbException, FormatException {
		if (type == REQUEST) {
			RequestMessage request = messageParser.parseRequestMessage(m, body);
			return engine.onRequestMessage(txn, session, request);
		} else if (type == ACCEPT) {
			AcceptMessage accept = messageParser.parseAcceptMessage(m, body);
			return engine.onAcceptMessage(txn, session, accept);
		} else if (type == DECLINE) {
			DeclineMessage decline = messageParser.parseDeclineMessage(m, body);
			return engine.onDeclineMessage(txn, session, decline);
		} else if (type == AUTH) {
			AuthMessage auth = messageParser.parseAuthMessage(m, body);
			return engine.onAuthMessage(txn, session, auth);
		} else if (type == ACTIVATE) {
			ActivateMessage activate =
					messageParser.parseActivateMessage(m, body);
			return engine.onActivateMessage(txn, session, activate);
		} else if (type == ABORT) {
			AbortMessage abort = messageParser.parseAbortMessage(m, body);
			return engine.onAbortMessage(txn, session, abort);
		} else {
			throw new AssertionError();
		}
	}
	@Nullable
	private StoredSession getSession(Transaction txn,
			@Nullable SessionId sessionId) throws DbException, FormatException {
		if (sessionId == null) return null;
		BdfDictionary query = sessionParser.getSessionQuery(sessionId);
		Map<MessageId, BdfDictionary> results = clientHelper
				.getMessageMetadataAsDictionary(txn, localGroup.getId(), query);
		if (results.size() > 1) throw new DbException();
		if (results.isEmpty()) return null;
		return new StoredSession(results.keySet().iterator().next(),
				results.values().iterator().next());
	}
	private MessageId createStorageId(Transaction txn) throws DbException {
		Message m = clientHelper
				.createMessageForStoringMetadata(localGroup.getId());
		db.addLocalMessage(txn, m, new Metadata(), false, false);
		return m.getId();
	}
	private void storeSession(Transaction txn, MessageId storageId,
			Session<?> session) throws DbException {
		BdfDictionary d;
		if (session.getRole() == INTRODUCER) {
			d = sessionEncoder
					.encodeIntroducerSession((IntroducerSession) session);
		} else if (session.getRole() == INTRODUCEE) {
			d = sessionEncoder
					.encodeIntroduceeSession((IntroduceeSession) session);
		} else {
			throw new AssertionError();
		}
		try {
			clientHelper.mergeMessageMetadata(txn, storageId, d);
		} catch (FormatException e) {
			throw new AssertionError();
		}
	}
	@Override
	public boolean canIntroduce(Contact c1, Contact c2) throws DbException {
		return db.transactionWithResult(true,
				txn -> canIntroduce(txn, c1, c2));
	}
	public boolean canIntroduce(Transaction txn, Contact c1, Contact c2)
			throws DbException {
		try {
			Author introducer = identityManager.getLocalAuthor(txn);
			SessionId sessionId =
					crypto.getSessionId(introducer, c1.getAuthor(),
							c2.getAuthor());
			StoredSession ss = getSession(txn, sessionId);
			if (ss == null) return true;
			IntroducerSession session =
					sessionParser.parseIntroducerSession(ss.bdfSession);
			return session.getState().isComplete();
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	public void makeIntroduction(Contact c1, Contact c2, @Nullable String text)
			throws DbException {
		db.transaction(false,
				txn -> makeIntroduction(txn, c1, c2, text));
	}
	@Override
	public void makeIntroduction(Transaction txn, Contact c1, Contact c2,
			@Nullable String text) throws DbException {
		try {
			Author introducer = identityManager.getLocalAuthor(txn);
			SessionId sessionId =
					crypto.getSessionId(introducer, c1.getAuthor(),
							c2.getAuthor());
			StoredSession ss = getSession(txn, sessionId);
			IntroducerSession session;
			MessageId storageId;
			if (ss == null) {
				GroupId groupId1 = getContactGroup(c1).getId();
				GroupId groupId2 = getContactGroup(c2).getId();
				boolean alice = crypto.isAlice(c1.getAuthor().getId(),
						c2.getAuthor().getId());
				session = new IntroducerSession(sessionId,
						alice ? groupId1 : groupId2,
						alice ? c1.getAuthor() : c2.getAuthor(),
						alice ? groupId2 : groupId1,
						alice ? c2.getAuthor() : c1.getAuthor()
				);
				storageId = createStorageId(txn);
			} else {
				session = sessionParser.parseIntroducerSession(ss.bdfSession);
				storageId = ss.storageId;
			}
			session = introducerEngine.onRequestAction(txn, session, text);
			storeSession(txn, storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public void respondToIntroduction(ContactId contactId, SessionId sessionId,
			boolean accept) throws DbException {
		respondToIntroduction(contactId, sessionId, accept, false);
	}
	@Override
	public void respondToIntroduction(Transaction txn, ContactId contactId,
			SessionId sessionId, boolean accept) throws DbException {
		respondToIntroduction(txn, contactId, sessionId, accept, false);
	}
	private void respondToIntroduction(ContactId contactId, SessionId sessionId,
			boolean accept, boolean isAutoDecline) throws DbException {
		db.transaction(false,
				txn -> respondToIntroduction(txn, contactId, sessionId, accept,
						isAutoDecline));
	}
	private void respondToIntroduction(Transaction txn, ContactId contactId,
			SessionId sessionId, boolean accept, boolean isAutoDecline)
			throws DbException {
		try {
			StoredSession ss = getSession(txn, sessionId);
			if (ss == null) {
				throw new DbException();
			}
			Contact contact = db.getContact(txn, contactId);
			GroupId contactGroupId = getContactGroup(contact).getId();
			IntroduceeSession session = sessionParser
					.parseIntroduceeSession(contactGroupId, ss.bdfSession);
			if (accept) {
				session = introduceeEngine.onAcceptAction(txn, session);
			} else {
				session = introduceeEngine
						.onDeclineAction(txn, session, isAutoDecline);
			}
			storeSession(txn, ss.storageId, session);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public Collection<ConversationMessageHeader> getMessageHeaders(
			Transaction txn, ContactId c) throws DbException {
		try {
			Contact contact = db.getContact(txn, c);
			GroupId contactGroupId = getContactGroup(contact).getId();
			BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
			Map<MessageId, BdfDictionary> results = clientHelper
					.getMessageMetadataAsDictionary(txn, contactGroupId, query);
			List<ConversationMessageHeader> messages =
					new ArrayList<>(results.size());
			Map<AuthorId, AuthorInfo> authorInfos = new HashMap<>();
			for (Entry<MessageId, BdfDictionary> e : results.entrySet()) {
				MessageId m = e.getKey();
				MessageMetadata meta =
						messageParser.parseMetadata(e.getValue());
				MessageStatus status = db.getMessageStatus(txn, c, m);
				StoredSession ss = getSession(txn, meta.getSessionId());
				if (ss == null) throw new AssertionError();
				MessageType type = meta.getMessageType();
				if (type == REQUEST) {
					messages.add(parseInvitationRequest(txn, contactGroupId, m,
							meta, status, meta.getSessionId(), authorInfos));
				} else if (type == ACCEPT) {
					messages.add(parseInvitationResponse(txn, contactGroupId, m,
							meta, status, ss.bdfSession, authorInfos, true));
				} else if (type == DECLINE) {
					messages.add(parseInvitationResponse(txn, contactGroupId, m,
							meta, status, ss.bdfSession, authorInfos, false));
				}
			}
			return messages;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private IntroductionRequest parseInvitationRequest(Transaction txn,
			GroupId contactGroupId, MessageId m, MessageMetadata meta,
			MessageStatus status, SessionId sessionId,
			Map<AuthorId, AuthorInfo> authorInfos)
			throws DbException, FormatException {
		Message msg = clientHelper.getMessage(txn, m);
		BdfList body = clientHelper.toList(msg);
		RequestMessage rm = messageParser.parseRequestMessage(msg, body);
		String text = rm.getText();
		Author author = rm.getAuthor();
		AuthorInfo authorInfo = authorInfos.get(author.getId());
		if (authorInfo == null) {
			authorInfo = authorManager.getAuthorInfo(txn, author.getId());
			authorInfos.put(author.getId(), authorInfo);
		}
		return new IntroductionRequest(m, contactGroupId, meta.getTimestamp(),
				meta.isLocal(), meta.isRead(), status.isSent(), status.isSeen(),
				sessionId, author, text, !meta.isAvailableToAnswer(),
				authorInfo, rm.getAutoDeleteTimer());
	}
	private IntroductionResponse parseInvitationResponse(Transaction txn,
			GroupId contactGroupId, MessageId m, MessageMetadata meta,
			MessageStatus status, BdfDictionary bdfSession,
			Map<AuthorId, AuthorInfo> authorInfos, boolean accept)
			throws FormatException, DbException {
		Role role = sessionParser.getRole(bdfSession);
		SessionId sessionId;
		Author author;
		boolean canSucceed;
		if (role == INTRODUCER) {
			IntroducerSession session =
					sessionParser.parseIntroducerSession(bdfSession);
			sessionId = session.getSessionId();
			if (contactGroupId.equals(session.getIntroduceeA().groupId)) {
				author = session.getIntroduceeB().author;
			} else {
				author = session.getIntroduceeA().author;
			}
			IntroducerState s = session.getState();
			canSucceed = s != START && s != A_DECLINED && s != B_DECLINED;
		} else if (role == INTRODUCEE) {
			IntroduceeSession session = sessionParser
					.parseIntroduceeSession(contactGroupId, bdfSession);
			sessionId = session.getSessionId();
			author = session.getRemote().author;
			IntroduceeState s = session.getState();
			canSucceed = s != IntroduceeState.START && s != REMOTE_DECLINED;
		} else throw new AssertionError();
		AuthorInfo authorInfo = authorInfos.get(author.getId());
		if (authorInfo == null) {
			authorInfo = authorManager.getAuthorInfo(txn, author.getId());
			authorInfos.put(author.getId(), authorInfo);
		}
		return new IntroductionResponse(m, contactGroupId, meta.getTimestamp(),
				meta.isLocal(), meta.isRead(), status.isSent(), status.isSeen(),
				sessionId, accept, author, authorInfo, role, canSucceed,
				meta.getAutoDeleteTimer(), meta.isAutoDecline());
	}
	private void removeSessionWithIntroducer(Transaction txn,
			Contact introducer) throws DbException {
		BdfDictionary query = sessionEncoder
				.getIntroduceeSessionsByIntroducerQuery(introducer.getAuthor());
		Collection<MessageId> sessionIds;
		try {
			sessionIds = clientHelper.getMessageIds(txn, localGroup.getId(),
					query);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		for (MessageId id : sessionIds) {
			db.removeMessage(txn, id);
		}
	}
	private void abortOrRemoveSessionWithIntroducee(Transaction txn,
			Contact c) throws DbException {
		BdfDictionary query = sessionEncoder.getIntroducerSessionsQuery();
		Map<MessageId, BdfDictionary> sessions;
		try {
			sessions = clientHelper
					.getMessageMetadataAsDictionary(txn, localGroup.getId(),
							query);
		} catch (FormatException e) {
			throw new DbException();
		}
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		for (Entry<MessageId, BdfDictionary> session : sessions.entrySet()) {
			IntroducerSession s;
			try {
				s = sessionParser.parseIntroducerSession(session.getValue());
			} catch (FormatException e) {
				throw new DbException();
			}
			if (s.getIntroduceeA().author.equals(c.getAuthor())) {
				abortOrRemoveSessionWithIntroducee(txn, s, session.getKey(),
						s.getIntroduceeB(), localAuthor);
			} else if (s.getIntroduceeB().author.equals(c.getAuthor())) {
				abortOrRemoveSessionWithIntroducee(txn, s, session.getKey(),
						s.getIntroduceeA(), localAuthor);
			}
		}
	}
	private void abortOrRemoveSessionWithIntroducee(Transaction txn,
			IntroducerSession s, MessageId storageId, Introducee i,
			LocalAuthor localAuthor) throws DbException {
		if (db.containsContact(txn, i.author.getId(), localAuthor.getId())) {
			IntroducerSession session =
					introducerEngine.onIntroduceeRemoved(txn, i, s);
			storeSession(txn, storageId, session);
		} else {
			db.removeMessage(txn, storageId);
		}
	}
	@Override
	public void deleteMessages(Transaction txn, GroupId g,
			Collection<MessageId> messageIds) throws DbException {
		ContactId c;
		Map<SessionId, DeletableSession> sessions = new HashMap<>();
		try {
			c = clientHelper.getContactId(txn, g);
			for (MessageId messageId : messageIds) {
				BdfDictionary d = clientHelper
						.getMessageMetadataAsDictionary(txn, messageId);
				MessageMetadata messageMetadata =
						messageParser.parseMetadata(d);
				SessionId sessionId = messageMetadata.getSessionId();
				DeletableSession deletableSession =
						sessions.get(sessionId);
				if (deletableSession == null) {
					StoredSession ss = getSession(txn, sessionId);
					if (ss == null) throw new DbException();
					Role role = sessionParser.getRole(ss.bdfSession);
					Session session;
					if (role == INTRODUCER) {
						session = sessionParser
								.parseIntroducerSession(ss.bdfSession);
					} else if (role == INTRODUCEE) {
						session = sessionParser
								.parseIntroduceeSession(g, ss.bdfSession);
					} else throw new AssertionError();
					deletableSession = new DeletableSession(session.getState());
					sessions.put(sessionId, deletableSession);
				}
				deletableSession.messages.add(messageId);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		for (Entry<SessionId, DeletableSession> entry : sessions.entrySet()) {
			DeletableSession session = entry.getValue();
			if (session.state instanceof IntroduceeState) {
				IntroduceeState introduceeState =
						(IntroduceeState) session.state;
				if (introduceeState == AWAIT_RESPONSES ||
						introduceeState == REMOTE_DECLINED ||
						introduceeState == REMOTE_ACCEPTED) {
					respondToIntroduction(txn, c, entry.getKey(), false, true);
				}
			}
			for (MessageId m : session.messages) {
				db.deleteMessage(txn, m);
				db.deleteMessageMetadata(txn, m);
			}
		}
		recalculateGroupCount(txn, g);
		txn.attach(new ConversationMessagesDeletedEvent(c, messageIds));
	}
	@FunctionalInterface
	private interface MessageRetriever {
		Set<MessageId> getMessages(Set<MessageId> allMessages);
	}
	@Override
	public DeletionResult deleteAllMessages(Transaction txn, ContactId c)
			throws DbException {
		return deleteMessages(txn, c, allMessages -> allMessages);
	}
	@Override
	public DeletionResult deleteMessages(Transaction txn, ContactId c,
			Set<MessageId> messageIds) throws DbException {
		return deleteMessages(txn, c, allMessages -> messageIds);
	}
	private DeletionResult deleteMessages(Transaction txn, ContactId c,
			MessageRetriever retriever) throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		Map<MessageId, BdfDictionary> messages;
		try {
			messages = clientHelper.getMessageMetadataAsDictionary(txn, g);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		Set<MessageId> selected = retriever.getMessages(messages.keySet());
		Map<SessionId, DeletableSession> sessions = new HashMap<>();
		for (MessageId id : selected) {
			BdfDictionary d = messages.get(id);
			if (d == null) continue;
			MessageMetadata m;
			try {
				m = messageParser.parseMetadata(d);
			} catch (FormatException e) {
				throw new DbException(e);
			}
			if (m.getSessionId() == null) {
				throw new AssertionError("missing session ID");
			}
			DeletableSession session = sessions.get(m.getSessionId());
			if (session == null) {
				session = getDeletableSession(txn, g, m.getSessionId());
				sessions.put(m.getSessionId(), session);
			}
			session.messages.add(id);
		}
		for (Entry<MessageId, BdfDictionary> entry : messages.entrySet()) {
			if (selected.contains(entry.getKey())) continue;
			MessageMetadata m;
			try {
				m = messageParser.parseMetadata(entry.getValue());
			} catch (FormatException e) {
				throw new DbException(e);
			}
			if (!m.isVisibleInConversation()) continue;
			if (m.getSessionId() == null) {
				throw new AssertionError("missing session ID");
			}
			DeletableSession session = sessions.get(m.getSessionId());
			if (session == null) continue;
			session.messages.add(entry.getKey());
		}
		Set<MessageId> notAcked = new HashSet<>();
		for (MessageStatus status : db.getMessageStatus(txn, c, g)) {
			if (!status.isSeen()) notAcked.add(status.getMessageId());
		}
		DeletionResult result =
				deleteCompletedSessions(txn, sessions, notAcked, selected);
		recalculateGroupCount(txn, g);
		return result;
	}
	private DeletableSession getDeletableSession(Transaction txn,
			GroupId introducerGroupId, SessionId sessionId) throws DbException {
		try {
			StoredSession ss = getSession(txn, sessionId);
			if (ss == null) throw new AssertionError();
			Session<?> s;
			Role role = sessionParser.getRole(ss.bdfSession);
			if (role == INTRODUCER) {
				s = sessionParser.parseIntroducerSession(ss.bdfSession);
			} else if (role == INTRODUCEE) {
				s = sessionParser.parseIntroduceeSession(introducerGroupId,
						ss.bdfSession);
			} else throw new AssertionError();
			return new DeletableSession(s.getState());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private DeletionResult deleteCompletedSessions(Transaction txn,
			Map<SessionId, DeletableSession> sessions, Set<MessageId> notAcked,
			Set<MessageId> selected) throws DbException {
		DeletionResult result = new DeletionResult();
		for (DeletableSession session : sessions.values()) {
			if (!session.state.isComplete()) {
				result.addIntroductionSessionInProgress();
				continue;
			}
			boolean sessionDeletable = true;
			for (MessageId m : session.messages) {
				if (notAcked.contains(m) || !selected.contains(m)) {
					sessionDeletable = false;
					if (notAcked.contains(m))
						result.addIntroductionSessionInProgress();
					if (!selected.contains(m))
						result.addIntroductionNotAllSelected();
				}
			}
			if (sessionDeletable) {
				for (MessageId m : session.messages) {
					db.deleteMessage(txn, m);
					db.deleteMessageMetadata(txn, m);
				}
			}
		}
		return result;
	}
	@Override
	public Set<MessageId> getMessageIds(Transaction txn, ContactId c)
			throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
		try {
			return new HashSet<>(clientHelper.getMessageIds(txn, g, query));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private void recalculateGroupCount(Transaction txn, GroupId g)
			throws DbException {
		BdfDictionary query = messageParser.getMessagesVisibleInUiQuery();
		Map<MessageId, BdfDictionary> results;
		try {
			results =
					clientHelper.getMessageMetadataAsDictionary(txn, g, query);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		int msgCount = 0;
		int unreadCount = 0;
		for (Entry<MessageId, BdfDictionary> entry : results.entrySet()) {
			MessageMetadata meta;
			try {
				meta = messageParser.parseMetadata(entry.getValue());
			} catch (FormatException e) {
				throw new DbException(e);
			}
			msgCount++;
			if (!meta.isRead()) unreadCount++;
		}
		messageTracker.resetGroupCount(txn, g, msgCount, unreadCount);
	}
	private static class StoredSession {
		private final MessageId storageId;
		private final BdfDictionary bdfSession;
		private StoredSession(MessageId storageId, BdfDictionary bdfSession) {
			this.storageId = storageId;
			this.bdfSession = bdfSession;
		}
	}
	private static class DeletableSession {
		private final State state;
		private final List<MessageId> messages = new ArrayList<>();
		private DeletableSession(State state) {
			this.state = state;
		}
	}
}
