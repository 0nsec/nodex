package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.ClientId;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogManager.RemoveBlogHook;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
@Immutable
@NotNullByDefault
class BlogSharingManagerImpl extends SharingManagerImpl<Blog>
		implements BlogSharingManager, RemoveBlogHook {
	private final IdentityManager identityManager;
	private final BlogManager blogManager;
	@Inject
	BlogSharingManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser, MessageParser<Blog> messageParser,
			SessionEncoder sessionEncoder, SessionParser sessionParser,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			ProtocolEngine<Blog> engine,
			InvitationFactory<Blog, BlogInvitationResponse> invitationFactory,
			IdentityManager identityManager, BlogManager blogManager) {
		super(db, clientHelper, clientVersioningManager, metadataParser,
				messageParser, sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory);
		this.identityManager = identityManager;
		this.blogManager = blogManager;
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
		return BlogManager.CLIENT_ID;
	}
	@Override
	protected int getShareableMajorVersion() {
		return BlogManager.MAJOR_VERSION;
	}
	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		super.addingContact(txn, c);
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		Blog ourBlog = blogManager.getPersonalBlog(localAuthor);
		Blog theirBlog = blogManager.getPersonalBlog(c.getAuthor());
		try {
			preShareGroup(txn, c, ourBlog.getGroup());
			preShareGroup(txn, c, theirBlog.getGroup());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public void removingBlog(Transaction txn, Blog b) throws DbException {
		removingShareable(txn, b);
	}
}
