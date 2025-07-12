package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
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
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.event.GroupInvitationResponseReceivedEvent;
import org.nodex.api.privategroup.invitation.GroupInvitationResponse;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static java.lang.Math.max;
import static org.nodex.core.api.sync.GroupConstants.Visibility.INVISIBLE;
import static org.nodex.core.api.sync.GroupConstants.Visibility.SHARED;
import static org.nodex.privategroup.invitation.CreatorState.DISSOLVED;
import static org.nodex.privategroup.invitation.CreatorState.ERROR;
import static org.nodex.privategroup.invitation.CreatorState.INVITED;
import static org.nodex.privategroup.invitation.CreatorState.JOINED;
import static org.nodex.privategroup.invitation.CreatorState.LEFT;
import static org.nodex.privategroup.invitation.CreatorState.START;
@Immutable
@NotNullByDefault
class CreatorProtocolEngine extends AbstractProtocolEngine<CreatorSession> {
	CreatorProtocolEngine(
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
	public CreatorSession onInviteAction(Transaction txn, CreatorSession s,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) throws DbException {
		switch (s.getState()) {
			case START:
				return onLocalInvite(txn, s, text, timestamp, signature,
						autoDeleteTimer);
			case INVITED:
			case JOINED:
			case LEFT:
			case DISSOLVED:
			case ERROR:
				throw new ProtocolStateException();
			default:
				throw new AssertionError();
		}
	}
	@Override
	public CreatorSession onJoinAction(Transaction txn, CreatorSession s) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CreatorSession onLeaveAction(Transaction txn, CreatorSession s,
			boolean isAutoDecline) throws DbException {
		switch (s.getState()) {
			case START:
			case DISSOLVED:
			case ERROR:
				return s;
			case INVITED:
			case JOINED:
			case LEFT:
				return onLocalLeave(txn, s);
			default:
				throw new AssertionError();
		}
	}
	@Override
	public CreatorSession onMemberAddedAction(Transaction txn,
			CreatorSession s) {
		return s;
	}
	@Override
	public CreatorSession onInviteMessage(Transaction txn, CreatorSession s,
			InviteMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}
	@Override
	public CreatorSession onJoinMessage(Transaction txn, CreatorSession s,
			JoinMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case JOINED:
			case LEFT:
				return abort(txn, s);
			case INVITED:
				return onRemoteAccept(txn, s, m);
			case DISSOLVED:
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public CreatorSession onLeaveMessage(Transaction txn, CreatorSession s,
			LeaveMessage m) throws DbException, FormatException {
		switch (s.getState()) {
			case START:
			case LEFT:
				return abort(txn, s);
			case INVITED:
				return onRemoteDecline(txn, s, m);
			case JOINED:
				return onRemoteLeave(txn, s, m);
			case DISSOLVED:
			case ERROR:
				return s;
			default:
				throw new AssertionError();
		}
	}
	@Override
	public CreatorSession onAbortMessage(Transaction txn, CreatorSession s,
			AbortMessage m) throws DbException, FormatException {
		return abort(txn, s);
	}
	private CreatorSession onLocalInvite(Transaction txn, CreatorSession s,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) throws DbException {
		Message sent = sendInviteMessage(txn, s, text, timestamp, signature,
				autoDeleteTimer);
		conversationManager.trackOutgoingMessage(txn, sent);
		long localTimestamp =
				max(timestamp, getTimestampForVisibleMessage(txn, s));
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), localTimestamp,
				timestamp, INVITED);
	}
	private CreatorSession onLocalLeave(Transaction txn, CreatorSession s)
			throws DbException {
		try {
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		Message sent = sendLeaveMessage(txn, s);
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), DISSOLVED);
	}
	private CreatorSession onRemoteAccept(Transaction txn, CreatorSession s,
			JoinMessage m) throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		Message sent = sendJoinMessage(txn, s, false);
		markMessageVisibleInUi(txn, m.getId());
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		receiveAutoDeleteTimer(txn, m);
		setPrivateGroupVisibility(txn, s, SHARED);
		ContactId contactId =
				clientHelper.getContactId(txn, m.getContactGroupId());
		txn.attach(new GroupInvitationResponseReceivedEvent(
				createInvitationResponse(m, true), contactId));
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), m.getId(), sent.getTimestamp(),
				s.getInviteTimestamp(), JOINED);
	}
	private CreatorSession onRemoteDecline(Transaction txn, CreatorSession s,
			LeaveMessage m) throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		markMessageVisibleInUi(txn, m.getId());
		conversationManager.trackMessage(txn, m.getContactGroupId(),
				m.getTimestamp(), false);
		receiveAutoDeleteTimer(txn, m);
		ContactId contactId =
				clientHelper.getContactId(txn, m.getContactGroupId());
		txn.attach(new GroupInvitationResponseReceivedEvent(
				createInvitationResponse(m, false), contactId));
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), START);
	}
	private CreatorSession onRemoteLeave(Transaction txn, CreatorSession s,
			LeaveMessage m) throws DbException, FormatException {
		if (m.getTimestamp() <= s.getInviteTimestamp()) return abort(txn, s);
		if (!isValidDependency(s, m.getPreviousMessageId()))
			return abort(txn, s);
		setPrivateGroupVisibility(txn, s, INVISIBLE);
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				s.getLastLocalMessageId(), m.getId(), s.getLocalTimestamp(),
				s.getInviteTimestamp(), LEFT);
	}
	private CreatorSession abort(Transaction txn, CreatorSession s)
			throws DbException, FormatException {
		if (s.getState() == ERROR) return s;
		if (isSubscribedPrivateGroup(txn, s.getPrivateGroupId()))
			setPrivateGroupVisibility(txn, s, INVISIBLE);
		Message sent = sendAbortMessage(txn, s);
		return new CreatorSession(s.getContactGroupId(), s.getPrivateGroupId(),
				sent.getId(), s.getLastRemoteMessageId(), sent.getTimestamp(),
				s.getInviteTimestamp(), ERROR);
	}
	private GroupInvitationResponse createInvitationResponse(
			DeletableGroupInvitationMessage m, boolean accept) {
		SessionId sessionId = new SessionId(m.getPrivateGroupId().getBytes());
		return new GroupInvitationResponse(m.getId(), m.getContactGroupId(),
				m.getTimestamp(), false, false, false, false, sessionId,
				accept, m.getPrivateGroupId(), m.getAutoDeleteTimer(), false);
	}
}