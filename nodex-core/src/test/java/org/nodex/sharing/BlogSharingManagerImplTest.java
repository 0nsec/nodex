package org.nodex.sharing;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Metadata;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.core.test.BrambleMockTestCase;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.SessionId;
import org.jmock.Expectations;
import org.junit.Test;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.core.test.TestUtils.getAuthor;
import static org.nodex.core.test.TestUtils.getContact;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getLocalAuthor;
import static org.nodex.core.test.TestUtils.getMessage;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.api.blog.BlogSharingManager.CLIENT_ID;
import static org.nodex.api.blog.BlogSharingManager.MAJOR_VERSION;
public class BlogSharingManagerImplTest extends BrambleMockTestCase {
	private final BlogSharingManagerImpl blogSharingManager;
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final IdentityManager identityManager =
			context.mock(IdentityManager.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final ClientVersioningManager clientVersioningManager =
			context.mock(ClientVersioningManager.class);
	private final SessionEncoder sessionEncoder =
			context.mock(SessionEncoder.class);
	private final SessionParser sessionParser =
			context.mock(SessionParser.class);
	private final ContactGroupFactory contactGroupFactory =
			context.mock(ContactGroupFactory.class);
	private final BlogManager blogManager = context.mock(BlogManager.class);
	private final LocalAuthor localAuthor = getLocalAuthor();
	private final Author author = getAuthor();
	private final Contact contact =
			getContact(author, localAuthor.getId(), true);
	private final ContactId contactId = contact.getId();
	private final Collection<Contact> contacts =
			Collections.singletonList(contact);
	private final Group localGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final Group contactGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final Group blogGroup =
			getGroup(BlogManager.CLIENT_ID, BlogManager.MAJOR_VERSION);
	private final Blog blog = new Blog(blogGroup, author, false);
	private final Group localBlogGroup =
			getGroup(BlogManager.CLIENT_ID, BlogManager.MAJOR_VERSION);
	private final Blog localBlog = new Blog(localBlogGroup, localAuthor, false);
	@SuppressWarnings("unchecked")
	private final ProtocolEngine<Blog> engine =
			context.mock(ProtocolEngine.class);
	@SuppressWarnings("unchecked")
	public BlogSharingManagerImplTest() {
		MetadataParser metadataParser = context.mock(MetadataParser.class);
		MessageParser<Blog> messageParser = context.mock(MessageParser.class);
		MessageTracker messageTracker = context.mock(MessageTracker.class);
		InvitationFactory<Blog, BlogInvitationResponse> invitationFactory =
				context.mock(InvitationFactory.class);
		blogSharingManager = new BlogSharingManagerImpl(db, clientHelper,
				clientVersioningManager, metadataParser, messageParser,
				sessionEncoder, sessionParser, messageTracker,
				contactGroupFactory, engine, invitationFactory, identityManager,
				blogManager);
	}
	@Test
	public void testOpenDatabaseHookFirstTimeWithExistingContact()
			throws Exception {
		Transaction txn = new Transaction(null, false);
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory).createLocalGroup(CLIENT_ID,
					MAJOR_VERSION);
			will(returnValue(localGroup));
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(false));
			oneOf(db).addGroup(txn, localGroup);
			oneOf(db).getContacts(txn);
			will(returnValue(contacts));
		}});
		expectAddingContact(txn);
		blogSharingManager.onDatabaseOpened(txn);
	}
	private void expectAddingContact(Transaction txn) throws Exception {
		Map<MessageId, BdfDictionary> sessions = Collections.emptyMap();
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(db).addGroup(txn, contactGroup);
			oneOf(clientVersioningManager).getClientVisibility(txn, contactId,
					CLIENT_ID.toString(), MAJOR_VERSION);
			will(returnValue(SHARED));
			oneOf(db).setGroupVisibility(txn, contactId, contactGroup.getId(),
					SHARED);
			oneOf(clientHelper)
					.setContactId(txn, contactGroup.getId(), contactId);
			oneOf(identityManager).getLocalAuthor(txn);
			will(returnValue(localAuthor));
			oneOf(blogManager).getPersonalBlog(localAuthor);
			will(returnValue(localBlog));
			oneOf(blogManager).getPersonalBlog(author);
			will(returnValue(blog));
		}});
		expectPreShareShareable(txn, contact, localBlog, sessions);
		expectPreShareShareable(txn, contact, blog, sessions);
	}
	@Test
	public void testOpenDatabaseHookSubsequentTime() throws Exception {
		Transaction txn = new Transaction(null, false);
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory).createLocalGroup(CLIENT_ID,
					MAJOR_VERSION);
			will(returnValue(localGroup));
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(true));
		}});
		blogSharingManager.onDatabaseOpened(txn);
	}
	@Test
	public void testAddingContact() throws Exception {
		Transaction txn = new Transaction(null, false);
		expectAddingContact(txn);
		blogSharingManager.addingContact(txn, contact);
	}
	@Test
	public void testRemovingBlogFreshState() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(0);
		testRemovingBlog(sessions);
	}
	@Test
	public void testRemovingBlogExistingState() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(1);
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		testRemovingBlog(sessions);
	}
	@Test(expected = DbException.class)
	public void testRemovingBlogMultipleSessions() throws Exception {
		Map<MessageId, BdfDictionary> sessions = new HashMap<>(2);
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		sessions.put(new MessageId(getRandomId()), new BdfDictionary());
		testRemovingBlog(sessions);
	}
	private void expectPreShareShareable(Transaction txn, Contact contact,
			Blog blog, Map<MessageId, BdfDictionary> sessions)
			throws Exception {
		Group contactGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
		BdfDictionary sessionDict = new BdfDictionary();
		Message message = getMessage(contactGroup.getId());
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(sessionParser)
					.getSessionQuery(new SessionId(blog.getId().getBytes()));
			will(returnValue(sessionDict));
			oneOf(clientHelper).getMessageMetadataAsDictionary(txn,
					contactGroup.getId(), sessionDict);
			will(returnValue(sessions));
			if (sessions.size() == 0) {
				oneOf(db).addGroup(txn, blog.getGroup());
				oneOf(clientVersioningManager).getClientVisibility(txn,
						contactId, BlogManager.CLIENT_ID,
						BlogManager.MAJOR_VERSION);
				will(returnValue(SHARED));
				oneOf(db).setGroupVisibility(txn, contact.getId(),
						blog.getGroup().getId(), SHARED);
				oneOf(clientHelper)
						.createMessageForStoringMetadata(contactGroup.getId());
				will(returnValue(message));
				oneOf(db).addLocalMessage(txn, message, new Metadata(), false,
						false);
				oneOf(sessionEncoder).encodeSession(with(any(Session.class)));
				will(returnValue(sessionDict));
				oneOf(clientHelper).mergeMessageMetadata(txn, message.getId(),
						sessionDict);
			}
		}});
	}
	private void testRemovingBlog(Map<MessageId, BdfDictionary> sessions)
			throws Exception {
		Transaction txn = new Transaction(null, false);
		BdfDictionary sessionDict = new BdfDictionary();
		Session session = new Session(contactGroup.getId(), blog.getId());
		context.checking(new Expectations() {{
			oneOf(db).getContacts(txn);
			will(returnValue(contacts));
			oneOf(contactGroupFactory).createContactGroup(CLIENT_ID,
					MAJOR_VERSION, contact);
			will(returnValue(contactGroup));
			oneOf(sessionParser)
					.getSessionQuery(new SessionId(blog.getId().getBytes()));
			will(returnValue(sessionDict));
			oneOf(clientHelper).getMessageMetadataAsDictionary(txn,
					contactGroup.getId(), sessionDict);
			will(returnValue(sessions));
			if (sessions.size() == 1) {
				oneOf(sessionParser)
						.parseSession(contactGroup.getId(), sessionDict);
				will(returnValue(session));
				oneOf(engine).onLeaveAction(txn, session);
				will(returnValue(session));
				oneOf(sessionEncoder).encodeSession(session);
				will(returnValue(sessionDict));
				oneOf(clientHelper).mergeMessageMetadata(txn,
						sessions.keySet().iterator().next(), sessionDict);
			}
		}});
		blogSharingManager.removingBlog(txn, blog);
	}
}
