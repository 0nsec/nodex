package org.nodex.sharing;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DatabaseComponent;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.system.Clock;
import org.nodex.core.api.versioning.ClientVersioningManager;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.blog.event.BlogInvitationRequestReceivedEvent;
import org.nodex.api.blog.event.BlogInvitationResponseReceivedEvent;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
@Immutable
@NotNullByDefault
class BlogProtocolEngineImpl extends ProtocolEngineImpl<Blog> {
	private final BlogManager blogManager;
	private final InvitationFactory<Blog, BlogInvitationResponse>
			invitationFactory;
	@Inject
	BlogProtocolEngineImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MessageEncoder messageEncoder,
			MessageParser<Blog> messageParser,
			AutoDeleteManager autoDeleteManager,
			ConversationManager conversationManager,
			Clock clock,
			BlogManager blogManager,
			InvitationFactory<Blog, BlogInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, messageEncoder,
				messageParser, autoDeleteManager,
				conversationManager, clock, BlogSharingManager.CLIENT_ID,
				BlogSharingManager.MAJOR_VERSION, BlogManager.CLIENT_ID,
				BlogManager.MAJOR_VERSION);
		this.blogManager = blogManager;
		this.invitationFactory = invitationFactory;
	}
	@Override
	Event getInvitationRequestReceivedEvent(InviteMessage<Blog> m,
			ContactId contactId, boolean available, boolean canBeOpened) {
		ConversationRequest<Blog> request = invitationFactory
				.createInvitationRequest(false, false, true, false, m,
						contactId, available, canBeOpened,
						m.getAutoDeleteTimer());
		return new BlogInvitationRequestReceivedEvent(request, contactId);
	}
	@Override
	Event getInvitationResponseReceivedEvent(AcceptMessage m,
			ContactId contactId) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, false, false,
						true, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	Event getInvitationResponseReceivedEvent(DeclineMessage m,
			ContactId contactId) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), m.getContactGroupId(),
						m.getTimestamp(), false, false, false, false,
						false, m.getShareableId(), m.getAutoDeleteTimer(),
						false);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	Event getAutoDeclineInvitationResponseReceivedEvent(Session s, Message m,
			ContactId contactId, long timer) {
		BlogInvitationResponse response = invitationFactory
				.createInvitationResponse(m.getId(), s.getContactGroupId(),
						m.getTimestamp(), true, false, false, true,
						false, s.getShareableId(), timer, true);
		return new BlogInvitationResponseReceivedEvent(response, contactId);
	}
	@Override
	protected void addShareable(Transaction txn, MessageId inviteId)
			throws DbException, FormatException {
		InviteMessage<Blog> invite =
				messageParser.getInviteMessage(txn, inviteId);
		blogManager.addBlog(txn, invite.getShareable());
	}
}