package org.nodex.sharing;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumManager.RemoveForumHook;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.nullsafety.NotNullByDefault;
import javax.inject.Inject;
@NotNullByDefault
class ForumSharingManagerImpl extends SharingManagerImpl<Forum>
		implements ForumSharingManager, RemoveForumHook {
	@Inject
	ForumSharingManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageParser<Forum> messageParser,
			SessionEncoder sessionEncoder, SessionParser sessionParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ProtocolEngine<Forum> engine,
			InvitationFactory<Forum, ForumInvitationResponse> invitationFactory) {
		super(db, clientHelper, clientVersioningManager, metadataParser,
				messageParser, sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory);
	}
	@Override
	protected ClientId getClientId() {
		return CLIENT_ID;
	}
	@Override
	protected int getMajorVersion() {
		return MAJOR_VERSION;
	}
	@Override
	protected ClientId getShareableClientId() {
		return ForumManager.CLIENT_ID;
	}
	@Override
	protected int getShareableMajorVersion() {
		return ForumManager.MAJOR_VERSION;
	}
	@Override
	public void removingForum(Transaction txn, Forum f) throws DbException {
		removingShareable(txn, f);
	}
}