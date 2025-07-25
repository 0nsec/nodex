package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.event.GroupInvitationRequestReceivedEvent;
import org.nodex.api.privategroup.invitation.GroupInvitationRequest;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static org.nodex.api.sync.Group.Visibility.INVISIBLE;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.api.sync.Group.Visibility.VISIBLE;
import static org.nodex.privategroup.invitation.InviteeState.ACCEPTED;
import static org.nodex.privategroup.invitation.InviteeState.DISSOLVED;
import static org.nodex.privategroup.invitation.InviteeState.ERROR;
import static org.nodex.privategroup.invitation.InviteeState.INVITED;
import static org.nodex.privategroup.invitation.InviteeState.JOINED;
import static org.nodex.privategroup.invitation.InviteeState.LEFT;
import static org.nodex.privategroup.invitation.InviteeState.START;
@Immutable
@NotNullByDefault
class InviteeProtocolEngine extends AbstractProtocolEngine<InviteeSession> {
	InviteeProtocolEngine(
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
	public InviteeSession onInviteAction(Transaction txn, InviteeSession s,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) {
		throw new UnsupportedOperationException();
	}
	@Override
	public InviteeSession onJoinAction(Transaction txn, InviteeSession s)
			throws DbException {
		switch (s.getState()) {
			case START:
			case ACCEPTED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
			case ERROR:
				throw new ProtocolStateException();
			case INVITED:
				return onLocalAccept(txn, s);
			default:
				throw new AssertionError();
		}
	}
	@Override
	public InviteeSession onLeaveAction(Transaction txn, InviteeSession s,
			boolean isAutoDecline) throws DbException {
		switch (s.getState()) {
			case START:
			case LEFT:
			case DISSOLVED:
			case ERROR:
				return s;
			case INVITED:
				return onLocalDecline(txn, s, isAutoDecline);
			case ACCEPTED:
			case JOINED:
				return onLocalLeave(txn, s);
			default:
				throw new AssertionError();
		}
	}
	@Override
	public InviteeSession onMemberAddedAction(Transaction txn,
			InviteeSession s) {
		return s;
	}
	@Override
	public InviteeSession onInviteMessage(Transaction txn, InviteeSession s,
			InviteMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
				return onRemoteInvite(txn, s, m);
			case INVITED:
			case ACCEPTED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
				return abort(txn, s);
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public InviteeSession onJoinMessage(Transaction txn, InviteeSession s,
			JoinMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case INVITED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
				return abort(txn, s);
			case ACCEPTED:
				return onRemoteJoin(txn, s, m);
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public InviteeSession onLeaveMessage(Transaction txn, InviteeSession s,
			LeaveMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case DISSOLVED:
				return abort(txn, s);
			case INVITED:
			case LEFT:
				return onRemoteLeaveWhenNotSubscribed(txn, s, m);
			case ACCEPTED:
			case JOINED:
				return onRemoteLeaveWhenSubscribed(txn, s, m);
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public InviteeSession onAbortMessage(Transaction txn, InviteeSession s,
			AbortMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}
	private InviteeSession onLocalAccept(Transaction txn, InviteeSession s)
			throws DbException {
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		markInviteAccepted(txn, inviteId);
		Message sent = sendJoinMessage(txn, s, true);
		conversationManager.trackOutgoingMessage(txn, sent);
		try {
			subscribeToPrivateGroup(txn, inviteId);
			setPrivateGroupVisibility(txn, s, VISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), ACCEPTED);
	}
	private InviteeSession onLocalDecline(Transaction txn, InviteeSession s,
			boolean isAutoDecline) throws DbException {
		MessageId inviteId = s.getLastRemoteMessageId();
		if (inviteId == null) throw new IllegalStateException();
		markMessageAvailableToAnswer(txn, inviteId, false);
		Message sent = sendLeaveMessage(txn, s, true, isAutoDecline);
		conversationManager.trackOutgoingMessage(txn, sent);
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), START);
	}
	private InviteeSession onLocalLeave(Transaction txn, InviteeSession s)
			throws DbException {
		Message sent = sendLeaveMessage(txn, s);
		try {
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), LEFT);
	}
	private InviteeSession onRemoteInvite(Transaction txn, InviteeSession s,
			InviteMessage m) throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		ContactId contactId =
				clientHelper.getContactId(txn, s.getContactGroupId());
		Author contact = db.getContact(txn, contactId).getAuthor();
		if (!contact.getId().equals(m.getCreator().getId()))
			return abort(txn, s);
		markMessageVisibleInUi(txn, m.getId());
		markMessageAvailableToAnswer(txn, m.getId(), true);
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		receiveAutoDeleteTimer(txn, m);
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				m.getGroupName(), m.getCreator(), m.getSalt());
		txn.attach(new GroupInvitationRequestReceivedEvent(
				createInvitationRequest(m, privateGroup), contactId));
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				m.getTimestamp(), INVITED);
	}
	private InviteeSession onRemoteJoin(Transaction txn, InviteeSession s,
			JoinMessage m) throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		try {
			setPrivateGroupVisibility(txn, s, SHARED);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), JOINED);
	}
	private InviteeSession onRemoteLeaveWhenNotSubscribed(Transaction txn,
			InviteeSession s, LeaveMessage m)
			throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		markInvitesUnavailableToAnswer(txn, s);
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), DISSOLVED);
	}
	private InviteeSession onRemoteLeaveWhenSubscribed(Transaction txn,
			InviteeSession s, LeaveMessage m)
			throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		try {
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		privateGroupManager.markGroupDissolved(txn, s.getPrivateGroupId());
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), DISSOLVED);
	}
	private InviteeSession abort(Transaction txn, InviteeSession s)
			throws DbException, FormatException {
		if (s.getState() == ERROR) return s;
		markInvitesUnavailableToAnswer(txn, s);
		if (isSubscribedPrivateGroup(txn, s.getPrivateGroupId()))
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		Message sent = sendAbortMessage(txn, s);
		return new InviteeSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), ERROR);
	}
	private GroupInvitationRequest createInvitationRequest(InviteMessage m,
			PrivateGroup pg) {
		SessionId sessionId = new SessionId(m.getPrivateGroupId().getBytes());
		return new GroupInvitationRequest(m.getId(), m.getContactGroupId(),
				m.getTimestamp(), false, false, false, false, sessionId, pg,
				m.getText(), true, false, m.getAutoDeleteTimer());
	}
}
