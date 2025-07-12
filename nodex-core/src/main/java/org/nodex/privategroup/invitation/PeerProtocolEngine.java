package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.sync.Message;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static org.nodex.core.api.sync.GroupConstantsConstants.Visibility.INVISIBLE;
import static org.nodex.core.api.sync.GroupConstantsConstants.Visibility.SHARED;
import static org.nodex.core.api.sync.GroupConstantsConstants.Visibility.VISIBLE;
import static org.nodex.privategroup.invitation.PeerState.AWAIT_MEMBER;
import static org.nodex.privategroup.invitation.PeerState.BOTH_JOINED;
import static org.nodex.privategroup.invitation.PeerState.ERROR;
import static org.nodex.privategroup.invitation.PeerState.LOCAL_JOINED;
import static org.nodex.privategroup.invitation.PeerState.LOCAL_LEFT;
import static org.nodex.privategroup.invitation.PeerState.NEITHER_JOINED;
import static org.nodex.privategroup.invitation.PeerState.START;
@Immutable
@NotNullByDefault
class PeerProtocolEngine extends AbstractProtocolEngine<PeerSession> {
	PeerProtocolEngine(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			PrivateGroupManager privateGroupManager,
			PrivateGroupFactory privateGroupFactory,
			GroupMessageFactory groupMessageFactory,
			IdentityManager identityManager,
			MessageParser messageParser,
			MessageEncoder messageEncoder,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock) {
		super(db, clientHelper, clientVersioningManager, privateGroupManager,
				privateGroupFactory, groupMessageFactory, identityManager,
				messageParser, messageEncoder,
				autoDeleteManager, conversationManager, clock);
	}
	@Override
	public PeerSession onInviteAction(Transaction txn, PeerSession s,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) {
		throw new UnsupportedOperationException();
	}
	@Override
	public PeerSession onJoinAction(Transaction txn, PeerSession s)
			throws DbException {
		switch (s.getState()) {
			case START:
			case AWAIT_MEMBER:
			case LOCAL_JOINED:
			case BOTH_JOINED:
			case ERROR:
				throw new ProtocolStateException();
			case NEITHER_JOINED:
				return onLocalJoinFromNeitherJoined(txn, s);
			case LOCAL_LEFT:
				return onLocalJoinFromLocalLeft(txn, s);
			default:
				throw new AssertionError();
		}
	}
	@Override
	public PeerSession onLeaveAction(Transaction txn, PeerSession s,
			boolean isAutoDecline) throws DbException {
		switch (s.getState()) {
			case START:
			case AWAIT_MEMBER:
			case NEITHER_JOINED:
			case LOCAL_LEFT:
			case ERROR:
				return s;
			case LOCAL_JOINED:
				return onLocalLeaveFromLocalJoined(txn, s);
			case BOTH_JOINED:
				return onLocalLeaveFromBothJoined(txn, s);
			default:
				throw new AssertionError();
		}
	}
	@Override
	public PeerSession onMemberAddedAction(Transaction txn, PeerSession s)
			throws DbException {
		switch (s.getState()) {
			case START:
				return onMemberAddedFromStart(s);
			case AWAIT_MEMBER:
				return onMemberAddedFromAwaitMember(txn, s);
			case NEITHER_JOINED:
			case LOCAL_JOINED:
			case BOTH_JOINED:
			case LOCAL_LEFT:
				throw new ProtocolStateException();
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public PeerSession onInviteMessage(Transaction txn, PeerSession s,
			InviteMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}
	@Override
	public PeerSession onJoinMessage(Transaction txn, PeerSession s,
			JoinMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case AWAIT_MEMBER:
			case BOTH_JOINED:
			case LOCAL_LEFT:
				return abort(txn, s);
			case START:
				return onRemoteJoinFromStart(txn, s, m);
			case NEITHER_JOINED:
				return onRemoteJoinFromNeitherJoined(txn, s, m);
			case LOCAL_JOINED:
				return onRemoteJoinFromLocalJoined(txn, s, m);
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public PeerSession onLeaveMessage(Transaction txn, PeerSession s,
			LeaveMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case NEITHER_JOINED:
			case LOCAL_JOINED:
				return abort(txn, s);
			case AWAIT_MEMBER:
				return onRemoteLeaveFromAwaitMember(txn, s, m);
			case LOCAL_LEFT:
				return onRemoteLeaveFromLocalLeft(txn, s, m);
			case BOTH_JOINED:
				return onRemoteLeaveFromBothJoined(txn, s, m);
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public PeerSession onAbortMessage(Transaction txn, PeerSession s,
			AbortMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}
	private PeerSession onLocalJoinFromNeitherJoined(Transaction txn,
			PeerSession s) throws DbException {
		Message sent = sendJoinMessage(txn, s, false);
		try {
			setPrivateGroupVisibility(txn, s, VISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				LOCAL_JOINED);
	}
	private PeerSession onLocalJoinFromLocalLeft(Transaction txn, PeerSession s)
			throws DbException {
		Message sent = sendJoinMessage(txn, s, false);
		try {
			setPrivateGroupVisibility(txn, s, SHARED);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				BOTH_JOINED);
	}
	private PeerSession onLocalLeaveFromBothJoined(Transaction txn,
			PeerSession s) throws DbException {
		Message sent = sendLeaveMessage(txn, s);
		try {
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				LOCAL_LEFT);
	}
	private PeerSession onLocalLeaveFromLocalJoined(Transaction txn,
			PeerSession s) throws DbException {
		Message sent = sendLeaveMessage(txn, s);
		try {
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				NEITHER_JOINED);
	}
	private PeerSession onMemberAddedFromStart(PeerSession s) {
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), s.getLastRemoteMessageId(),
				s.getLocalTimestamp(), NEITHER_JOINED);
	}
	private PeerSession onMemberAddedFromAwaitMember(Transaction txn,
			PeerSession s) throws DbException {
		Message sent = sendJoinMessage(txn, s, false);
		try {
			setPrivateGroupVisibility(txn, s, SHARED);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		try {
			relationshipRevealed(txn, s, true);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				BOTH_JOINED);
	}
	private PeerSession onRemoteJoinFromStart(Transaction txn,
			PeerSession s, JoinMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				AWAIT_MEMBER);
	}
	private PeerSession onRemoteJoinFromNeitherJoined(Transaction txn,
			PeerSession s, JoinMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		Message sent = sendJoinMessage(txn, s, false);
		setPrivateGroupVisibility(txn, s, SHARED);
		relationshipRevealed(txn, s, true);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), m.getId(), sent.getTimestamp(), BOTH_JOINED);
	}
	private PeerSession onRemoteJoinFromLocalJoined(Transaction txn,
			PeerSession s, JoinMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		setPrivateGroupVisibility(txn, s, SHARED);
		relationshipRevealed(txn, s, false);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				BOTH_JOINED);
	}
	private PeerSession onRemoteLeaveFromAwaitMember(Transaction txn,
			PeerSession s, LeaveMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				START);
	}
	private PeerSession onRemoteLeaveFromLocalLeft(Transaction txn,
			PeerSession s, LeaveMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				NEITHER_JOINED);
	}
	private PeerSession onRemoteLeaveFromBothJoined(Transaction txn,
			PeerSession s, LeaveMessage m) throws DbException, FormatException {
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		setPrivateGroupVisibility(txn, s, VISIBLE);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				LOCAL_JOINED);
	}
	private PeerSession abort(Transaction txn, PeerSession s)
			throws DbException, FormatException {
		if (s.getState() == ERROR) return s;
		if (isSubscribedPrivateGroup(txn, s.getPrivateGroupId()))
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		Message sent = sendAbortMessage(txn, s);
		return new PeerSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				ERROR);
	}
	private void relationshipRevealed(Transaction txn, PeerSession s,
			boolean byContact) throws DbException, FormatException {
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		Contact contact = db.getContact(txn, contactId);
		privateGroupManager.relationshipRevealed(txn, s.getPrivateGroupId(),
				contact.getAuthor().getId(), byContact);
	}
}