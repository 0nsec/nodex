package org.nodex.privategroup.invitation;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.ProtocolStateException;
import org.jmock.Expectations;
import org.junit.Test;
import static org.nodex.api.sync.Group.Visibility.INVISIBLE;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.privategroup.invitation.CreatorState.DISSOLVED;
import static org.nodex.privategroup.invitation.CreatorState.ERROR;
import static org.nodex.privategroup.invitation.CreatorState.INVITED;
import static org.nodex.privategroup.invitation.CreatorState.JOINED;
import static org.nodex.privategroup.invitation.CreatorState.LEFT;
import static org.nodex.privategroup.invitation.CreatorState.START;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
public class CreatorProtocolEngineTest extends AbstractProtocolEngineTest {
	private final CreatorProtocolEngine engine =
			new CreatorProtocolEngine(db, clientHelper, clientVersioningManager,
					privateGroupManager, privateGroupFactory,
					groupMessageFactory, identityManager, messageParser,
					messageEncoder, autoDeleteManager,
					conversationManager, clock);
	private CreatorSession getDefaultSession(CreatorState state) {
		return new CreatorSession(contactGroupId, privateGroupId,
				lastLocalMessageId, lastRemoteMessageId, localTimestamp,
				inviteTimestamp, state);
	}
	@Test
	public void testOnInviteActionFromStart() throws Exception {
		CreatorSession session =
				new CreatorSession(contactGroupId, privateGroupId);
		String text = "Invitation text";
		expectOnLocalInvite(text);
		CreatorSession newSession =
				engine.onInviteAction(txn, session, text, inviteTimestamp,
						signature, NO_AUTO_DELETE_TIMER);
		assertEquals(INVITED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertNull(newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	@Test
	public void testOnInviteActionFromStartWithNullMessage() throws Exception {
		CreatorSession session =
				new CreatorSession(contactGroupId, privateGroupId);
		expectOnLocalInvite(null);
		CreatorSession newSession =
				engine.onInviteAction(txn, session, null, inviteTimestamp,
						signature, NO_AUTO_DELETE_TIMER);
		assertEquals(INVITED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertNull(newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	private void expectOnLocalInvite(String text) throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).getGroup(txn, privateGroupId);
			will(returnValue(privateGroupGroup));
			oneOf(privateGroupFactory).parsePrivateGroup(privateGroupGroup);
			will(returnValue(privateGroup));
			oneOf(conversationManager).trackOutgoingMessage(txn, message);
		}});
		expectSendInviteMessage(text);
	}
	@Test(expected = ProtocolStateException.class)
	public void testOnInviteActionFromInvited() throws Exception {
		engine.onInviteAction(txn, getDefaultSession(INVITED), null,
				inviteTimestamp, signature, NO_AUTO_DELETE_TIMER);
	}
	@Test(expected = ProtocolStateException.class)
	public void testOnInviteActionFromJoined() throws Exception {
		engine.onInviteAction(txn, getDefaultSession(JOINED), null,
				inviteTimestamp, signature, NO_AUTO_DELETE_TIMER);
	}
	@Test(expected = ProtocolStateException.class)
	public void testOnInviteActionFromLeft() throws Exception {
		engine.onInviteAction(txn, getDefaultSession(LEFT), null,
				inviteTimestamp, signature, NO_AUTO_DELETE_TIMER);
	}
	@Test(expected = ProtocolStateException.class)
	public void testOnInviteActionFromDissolved() throws Exception {
		engine.onInviteAction(txn, getDefaultSession(DISSOLVED), null,
				inviteTimestamp, signature, NO_AUTO_DELETE_TIMER);
	}
	@Test(expected = ProtocolStateException.class)
	public void testOnInviteActionFromError() throws Exception {
		engine.onInviteAction(txn, getDefaultSession(ERROR), null,
				inviteTimestamp, signature, NO_AUTO_DELETE_TIMER);
	}
	@Test(expected = UnsupportedOperationException.class)
	public void testOnJoinActionFails() {
		engine.onJoinAction(txn, getDefaultSession(START));
	}
	@Test
	public void testOnLeaveActionFromStart() throws Exception {
		CreatorSession session = getDefaultSession(START);
		assertEquals(session, engine.onLeaveAction(txn, session, false));
	}
	@Test
	public void testOnLeaveActionFromDissolved() throws Exception {
		CreatorSession session = getDefaultSession(DISSOLVED);
		assertEquals(session, engine.onLeaveAction(txn, session, false));
	}
	@Test
	public void testOnLeaveActionFromError() throws Exception {
		CreatorSession session = getDefaultSession(ERROR);
		assertEquals(session, engine.onLeaveAction(txn, session, false));
	}
	@Test
	public void testOnLeaveActionFromInvited() throws Exception {
		CreatorSession session = getDefaultSession(INVITED);
		expectOnLocalLeave();
		CreatorSession newSession = engine.onLeaveAction(txn, session, false);
		assertEquals(DISSOLVED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertEquals(lastRemoteMessageId, newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	@Test
	public void testOnLeaveActionFromJoined() throws Exception {
		CreatorSession session = getDefaultSession(JOINED);
		expectOnLocalLeave();
		CreatorSession newSession = engine.onLeaveAction(txn, session, false);
		assertEquals(DISSOLVED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertEquals(lastRemoteMessageId, newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	@Test
	public void testOnLeaveActionFromLeft() throws Exception {
		CreatorSession session = getDefaultSession(LEFT);
		expectOnLocalLeave();
		CreatorSession newSession = engine.onLeaveAction(txn, session, false);
		assertEquals(DISSOLVED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertEquals(lastRemoteMessageId, newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	private void expectOnLocalLeave() throws Exception {
		expectSetPrivateGroupVisibility(INVISIBLE);
		expectSendLeaveMessage(false);
	}
	@Test
	public void testOnMemberAddedAction() {
		CreatorSession session = getDefaultSession(START);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
		session = getDefaultSession(INVITED);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
		session = getDefaultSession(JOINED);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
		session = getDefaultSession(LEFT);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
		session = getDefaultSession(DISSOLVED);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
		session = getDefaultSession(ERROR);
		assertEquals(session, engine.onMemberAddedAction(txn, session));
	}
	@Test
	public void testOnInviteMessageInAnyStateWhenSubscribed() throws Exception {
		expectAbortWhenSubscribedToGroup();
		CreatorSession session = getDefaultSession(LEFT);
		CreatorSession newSession =
				engine.onInviteMessage(txn, session, inviteMessage);
		assertEquals(ERROR, newSession.getState());
		expectAbortWhenSubscribedToGroup();
		session = getDefaultSession(START);
		newSession =
				engine.onInviteMessage(txn, session, inviteMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnInviteMessageInAnyStateWhenNotSubscribed()
			throws Exception {
		expectAbortWhenNotSubscribedToGroup();
		CreatorSession session = getDefaultSession(LEFT);
		CreatorSession newSession =
				engine.onInviteMessage(txn, session, inviteMessage);
		assertEquals(ERROR, newSession.getState());
		expectAbortWhenNotSubscribedToGroup();
		session = getDefaultSession(START);
		newSession =
				engine.onInviteMessage(txn, session, inviteMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromStart() throws Exception {
		CreatorSession session = getDefaultSession(START);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, joinMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromJoined() throws Exception {
		CreatorSession session = getDefaultSession(JOINED);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, joinMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromLeft() throws Exception {
		CreatorSession session = getDefaultSession(LEFT);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, joinMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromInvitedWithWrongTimestamp()
			throws Exception {
		CreatorSession session = getDefaultSession(INVITED);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, joinMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromInvitedWithInvalidDependency()
			throws Exception {
		CreatorSession session = getDefaultSession(INVITED);
		JoinMessage invalidJoinMessage =
				new JoinMessage(messageId, contactGroupId, privateGroupId,
						inviteTimestamp + 1, messageId, NO_AUTO_DELETE_TIMER);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, invalidJoinMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromInvited() throws Exception {
		CreatorSession session = getDefaultSession(INVITED);
		JoinMessage properJoinMessage =
				new JoinMessage(new MessageId(getRandomId()), contactGroupId,
						privateGroupId, inviteTimestamp + 1,
						lastRemoteMessageId, NO_AUTO_DELETE_TIMER);
		expectSendJoinMessage(properJoinMessage, false);
		expectMarkMessageVisibleInUi(properJoinMessage.getId());
		expectTrackUnreadMessage(properJoinMessage.getTimestamp());
		expectReceiveAutoDeleteTimer(properJoinMessage);
		expectGetContactId();
		expectSetPrivateGroupVisibility(SHARED);
		CreatorSession newSession =
				engine.onJoinMessage(txn, session, properJoinMessage);
		assertEquals(JOINED, newSession.getState());
		assertEquals(messageId, newSession.getLastLocalMessageId());
		assertEquals(properJoinMessage.getId(),
				newSession.getLastRemoteMessageId());
		assertEquals(messageTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	@Test
	public void testOnJoinMessageFromDissolved() throws Exception {
		CreatorSession session = getDefaultSession(DISSOLVED);
		assertEquals(session, engine.onJoinMessage(txn, session, joinMessage));
	}
	@Test
	public void testOnJoinMessageFromError() throws Exception {
		CreatorSession session = getDefaultSession(ERROR);
		assertEquals(session, engine.onJoinMessage(txn, session, joinMessage));
	}
	@Test
	public void testOnLeaveMessageFromStart() throws Exception {
		LeaveMessage leaveMessage =
				new LeaveMessage(messageId, contactGroupId, privateGroupId,
						inviteTimestamp, lastLocalMessageId,
						NO_AUTO_DELETE_TIMER);
		CreatorSession session = getDefaultSession(START);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onLeaveMessage(txn, session, leaveMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnLeaveMessageFromLeft() throws Exception {
		CreatorSession session = getDefaultSession(LEFT);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onLeaveMessage(txn, session, leaveMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnLeaveMessageFromInvitedWithWrongTime() throws Exception {
		CreatorSession session = getDefaultSession(INVITED);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onLeaveMessage(txn, session, leaveMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnLeaveMessageFromInvitedWithWrongDependency()
			throws Exception {
		LeaveMessage invalidLeaveMessage =
				new LeaveMessage(messageId, contactGroupId, privateGroupId,
						inviteTimestamp + 1, lastLocalMessageId,
						NO_AUTO_DELETE_TIMER);
		CreatorSession session = getDefaultSession(INVITED);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onLeaveMessage(txn, session, invalidLeaveMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnLeaveMessageFromInvited()
			throws Exception {
		LeaveMessage properLeaveMessage =
				new LeaveMessage(new MessageId(getRandomId()), contactGroupId,
						privateGroupId, inviteTimestamp + 1,
						lastRemoteMessageId, NO_AUTO_DELETE_TIMER);
		CreatorSession session = getDefaultSession(INVITED);
		expectMarkMessageVisibleInUi(properLeaveMessage.getId());
		expectTrackUnreadMessage(properLeaveMessage.getTimestamp());
		expectReceiveAutoDeleteTimer(properLeaveMessage);
		expectGetContactId();
		CreatorSession newSession =
				engine.onLeaveMessage(txn, session, properLeaveMessage);
		assertEquals(START, newSession.getState());
		assertEquals(lastLocalMessageId, newSession.getLastLocalMessageId());
		assertEquals(properLeaveMessage.getId(),
				newSession.getLastRemoteMessageId());
		assertEquals(localTimestamp, newSession.getLocalTimestamp());
		assertEquals(inviteTimestamp, newSession.getInviteTimestamp());
		assertSessionConstantsUnchanged(session, newSession);
	}
	@Test
	public void testOnLeaveMessageFromDissolved() throws Exception {
		CreatorSession session = getDefaultSession(DISSOLVED);
		assertEquals(session,
				engine.onLeaveMessage(txn, session, leaveMessage));
	}
	@Test
	public void testOnLeaveMessageFromError() throws Exception {
		CreatorSession session = getDefaultSession(ERROR);
		assertEquals(session,
				engine.onLeaveMessage(txn, session, leaveMessage));
	}
	@Test
	public void testOnAbortMessageWhenNotSubscribed() throws Exception {
		CreatorSession session = getDefaultSession(START);
		expectIsNotSubscribedPrivateGroup();
		expectSendAbortMessage();
		CreatorSession newSession =
				engine.onAbortMessage(txn, session, abortMessage);
		assertSessionAborted(session, newSession);
	}
	@Test
	public void testOnAbortMessageWhenSubscribed() throws Exception {
		CreatorSession session = getDefaultSession(START);
		expectAbortWhenSubscribedToGroup();
		CreatorSession newSession =
				engine.onAbortMessage(txn, session, abortMessage);
		assertSessionAborted(session, newSession);
	}
	private void expectAbortWhenSubscribedToGroup() throws Exception {
		expectIsSubscribedPrivateGroup();
		expectSetPrivateGroupVisibility(INVISIBLE);
		expectSendAbortMessage();
	}
	private void expectAbortWhenNotSubscribedToGroup() throws Exception {
		expectIsNotSubscribedPrivateGroup();
		expectSendAbortMessage();
	}
	private void assertSessionAborted(CreatorSession oldSession,
			CreatorSession newSession) {
		assertEquals(ERROR, newSession.getState());
		assertSessionRecordedSentMessage(newSession);
		assertSessionConstantsUnchanged(oldSession, newSession);
	}
}
