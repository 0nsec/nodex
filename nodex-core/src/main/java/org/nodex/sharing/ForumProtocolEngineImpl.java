package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.event.Event;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationRequestReceivedEvent;
import org.nodex.api.forum.event.ForumInvitationResponseReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
@Immutable
@NotNullByDefault
class ForumProtocolEngineImpl extends ProtocolEngineImpl<Forum> {
	private final ForumManager forumManager;
	private final InvitationFactory<Forum, ForumInvitationResponse>
			invitationFactory;
	@Inject
	ForumProtocolEngineImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MessageEncoder messageEncoder,
			MessageParser<Forum> messageParser,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock,
			ForumManager forumManager,
			InvitationFactory<Forum, ForumInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, messageEncoder,
				messageParser, autoDeleteManager,
				conversationManager, clock, ForumSharingManager.CLIENT_ID,
				ForumSharingManager.MAJOR_VERSION, ForumManager.CLIENT_ID,
				ForumManager.MAJOR_VERSION);
		this.forumManager = forumManager;
		this.invitationFactory = invitationFactory;
	}
	@Override
	Event getInvitationRequestReceivedEvent(InviteMessage<Forum> m,
			ContactId contactId, boolean available, boolean canBeOpened) {
		ConversationRequest<Forum> request = invitationFactory
				.createInvitationRequest(false, false, true, false, m,
						contactId, available, canBeOpened,
						m.getAutoDeleteTimer());
		return new ForumInvitationRequestReceivedEvent(request, contactId);
	}
	@Override
	Event getInvitationResponseReceivedEvent(AcceptMessage m,
			ContactId contactId) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, true, false,
						true, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	Event getInvitationResponseReceivedEvent(DeclineMessage m,
			ContactId contactId) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, true, false,
						false, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	Event getAutoDeclineInvitationResponseReceivedEvent(Session s, Message m,
			ContactId contactId, long timer) {
		ForumInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), s.getContactGroupId(),
						m.getTimestamp(), true, false, false, true,
						false, s.getShareableId(), timer, true);
		return new ForumInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	protected void addShareable(Transaction txn, MessageId inviteId)
			throws DbException, FormatException {
		InviteMessage<Forum> invite =
				messageParser.getInviteMessage(txn, inviteId);
		forumManager.addForum(txn, invite.getShareable());
	}
}