package org.nodex.sharing;
import net.jodah.concurrentunit.Waiter;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchGroupException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogInvitationRequest;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.blog.event.BlogInvitationRequestReceivedEvent;
import org.nodex.api.blog.event.BlogInvitationResponseReceivedEvent;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.test.BriarIntegrationTest;
import org.nodex.test.BriarIntegrationTestComponent;
import org.nodex.test.DaggerBriarIntegrationTestComponent;
import org.nodex.nullsafety.NotNullByDefault;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Collection;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.blog.BlogSharingManager.CLIENT_ID;
import static org.nodex.api.blog.BlogSharingManager.MAJOR_VERSION;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHAREABLE;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHARING;
import static org.nodex.test.BriarTestUtils.assertGroupCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
public class BlogSharingIntegrationTest
		extends BriarIntegrationTest<BriarIntegrationTestComponent> {
	private BlogManager blogManager0, blogManager1;
	private Blog blog0, blog1, blog2, rssBlog;
	private SharerListener listener0;
	private InviteeListener listener1;
	private volatile BlogSharingManager blogSharingManager0;
	private volatile BlogSharingManager blogSharingManager1;
	private volatile BlogSharingManager blogSharingManager2;
	private volatile Waiter eventWaiter;
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		blogManager0 = c0.getBlogManager();
		blogManager1 = c1.getBlogManager();
		blogSharingManager0 = c0.getBlogSharingManager();
		blogSharingManager1 = c1.getBlogSharingManager();
		blogSharingManager2 = c2.getBlogSharingManager();
		blog0 = blogManager0.getPersonalBlog(author0);
		blog1 = blogManager0.getPersonalBlog(author1);
		blog2 = blogManager0.getPersonalBlog(author2);
		BlogFactory blogFactory = c0.getBlogFactory();
		rssBlog = blogFactory.createFeedBlog(author0);
		eventWaiter = new Waiter();
	}
	@Override
	protected void createComponents() {
		BriarIntegrationTestComponent component =
				DaggerBriarIntegrationTestComponent.builder().build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);
		c0 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c0);
		c1 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c1);
		c2 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c2);
	}
	@Test
	public void testPersonalBlogCannotBeSharedWithOwner() throws Exception {
		listenToEvents(true);
		assertEquals(SHARING,
				blogSharingManager0.getSharingStatus(blog1.getId(),
						contact1From0));
		assertEquals(SHARING,
				blogSharingManager0.getSharingStatus(blog2.getId(),
						contact2From0));
		assertEquals(SHARING,
				blogSharingManager1.getSharingStatus(blog0.getId(),
						contact0From1));
		assertEquals(SHARING,
				blogSharingManager2.getSharingStatus(blog0.getId(),
						contact0From2));
	}
	@Test
	public void testSuccessfulSharing() throws Exception {
		listenToEvents(true);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		assertEquals(2, blogManager1.getBlogs().size());
		GroupId g = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, contact1From0).getId();
		assertGroupCount(messageTracker0, g, 1, 0);
		Collection<ConversationMessageHeader> messages =
				db0.transactionWithResult(true, txn -> blogSharingManager0
						.getMessageHeaders(txn, contactId1From0));
		assertEquals(1, messages.size());
		assertMessageState(messages.iterator().next(), true, false, false);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		assertGroupCount(messageTracker1, g, 2, 1);
		messages = db1.transactionWithResult(true, txn -> blogSharingManager1
				.getMessageHeaders(txn, contactId0From1));
		assertEquals(2, messages.size());
		for (ConversationMessageHeader h : messages) {
			if (h instanceof ConversationResponse) {
				assertMessageState(h, true, false, false);
			}
		}
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertGroupCount(messageTracker0, g, 2, 1);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(3, blogManager1.getBlogs().size());
		assertTrue(blogManager1.getBlogs().contains(blog2));
		Collection<ConversationMessageHeader> list =
				db1.transactionWithResult(true, txn -> blogSharingManager1
						.getMessageHeaders(txn, contactId0From1));
		assertEquals(2, list.size());
		for (ConversationMessageHeader m : list) {
			if (m instanceof BlogInvitationRequest) {
				BlogInvitationRequest invitation = (BlogInvitationRequest) m;
				assertEquals(blog2, invitation.getNameable());
				assertTrue(invitation.wasAnswered());
				assertEquals(blog2.getAuthor().getName(),
						invitation.getName());
				assertFalse(invitation.getNameable().isRssFeed());
				assertEquals("Hi!", invitation.getText());
			} else {
				BlogInvitationResponse response = (BlogInvitationResponse) m;
				assertEquals(blog2.getId(), response.getShareableId());
				assertTrue(response.wasAccepted());
				assertTrue(response.isLocal());
			}
		}
		assertEquals(2, db0.transactionWithResult(true, txn ->
						blogSharingManager0.getMessageHeaders(txn, contactId1From0))
				.size());
		assertEquals(SHARING, blogSharingManager0.getSharingStatus(
				blog2.getId(), contact1From0));
		assertEquals(SHARING, blogSharingManager1.getSharingStatus(
				blog2.getId(), contact0From1));
		assertGroupCount(messageTracker0, g, 2, 1);
		assertGroupCount(messageTracker1, g, 2, 1);
	}
	@Test
	public void testSuccessfulSharingWithAutoDelete() throws Exception {
		listenToEvents(true);
		setAutoDeleteTimer(c0, contactId1From0, MIN_AUTO_DELETE_TIMER_MS);
		setAutoDeleteTimer(c1, contactId0From1, MIN_AUTO_DELETE_TIMER_MS);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(3, blogManager1.getBlogs().size());
		assertTrue(blogManager1.getBlogs().contains(blog2));
		for (ConversationMessageHeader h : getMessages1From0()) {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		}
		for (ConversationMessageHeader h : getMessages0From1()) {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		}
	}
	@Test
	public void testSuccessfulSharingWithRssBlog() throws Exception {
		listenToEvents(true);
		blogManager0.addBlog(rssBlog);
		blogSharingManager0
				.sendInvitation(rssBlog.getId(), contactId1From0, "Hi!");
		assertEquals(2, blogManager1.getBlogs().size());
		GroupId g = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, contact1From0).getId();
		assertGroupCount(messageTracker0, g, 1, 0);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		assertGroupCount(messageTracker1, g, 2, 1);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertGroupCount(messageTracker0, g, 2, 1);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(3, blogManager1.getBlogs().size());
		assertTrue(blogManager1.getBlogs().contains(rssBlog));
		Collection<ConversationMessageHeader> list =
				db1.transactionWithResult(true, txn -> blogSharingManager1
						.getMessageHeaders(txn, contactId0From1));
		assertEquals(2, list.size());
		for (ConversationMessageHeader m : list) {
			if (m instanceof BlogInvitationRequest) {
				BlogInvitationRequest invitation = (BlogInvitationRequest) m;
				assertEquals(rssBlog, invitation.getNameable());
				assertTrue(invitation.wasAnswered());
				assertEquals(rssBlog.getAuthor().getName(),
						invitation.getName());
				assertTrue(invitation.getNameable().isRssFeed());
				assertEquals("Hi!", invitation.getText());
			} else {
				BlogInvitationResponse response = (BlogInvitationResponse) m;
				assertEquals(rssBlog.getId(), response.getShareableId());
				assertTrue(response.wasAccepted());
				assertTrue(response.isLocal());
			}
		}
		assertEquals(2, db0.transactionWithResult(true, txn ->
						blogSharingManager0.getMessageHeaders(txn, contactId1From0))
				.size());
		assertEquals(SHARING, blogSharingManager0.getSharingStatus(
				rssBlog.getId(), contact1From0));
		assertEquals(SHARING, blogSharingManager1.getSharingStatus(
				rssBlog.getId(), contact0From1));
		assertGroupCount(messageTracker0, g, 2, 1);
		assertGroupCount(messageTracker1, g, 2, 1);
	}
	@Test
	public void testDeclinedSharing() throws Exception {
		listenToEvents(false);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(2, blogManager1.getBlogs().size());
		assertEquals(0, blogSharingManager1.getInvitations().size());
		Collection<ConversationMessageHeader> list =
				db1.transactionWithResult(true, txn -> blogSharingManager1
						.getMessageHeaders(txn, contactId0From1));
		assertEquals(2, list.size());
		for (ConversationMessageHeader m : list) {
			if (m instanceof BlogInvitationRequest) {
				BlogInvitationRequest invitation = (BlogInvitationRequest) m;
				assertEquals(blog2, invitation.getNameable());
				assertTrue(invitation.wasAnswered());
				assertEquals(blog2.getAuthor().getName(),
						invitation.getName());
				assertNull(invitation.getText());
			} else {
				BlogInvitationResponse response = (BlogInvitationResponse) m;
				assertEquals(blog2.getId(), response.getShareableId());
				assertFalse(response.wasAccepted());
				assertTrue(response.isLocal());
			}
		}
		assertEquals(2, db0.transactionWithResult(true, txn ->
						blogSharingManager0.getMessageHeaders(txn, contactId1From0))
				.size());
		assertEquals(SHAREABLE, blogSharingManager0.getSharingStatus(
				blog2.getId(), contact1From0));
	}
	@Test
	public void testInviteeLeavesAfterFinished() throws Exception {
		listenToEvents(true);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(3, blogManager1.getBlogs().size());
		assertTrue(blogManager1.getBlogs().contains(blog2));
		assertTrue(blogSharingManager0.getSharedWith(blog2.getId())
				.contains(contact1From0));
		assertTrue(blogSharingManager1.getSharedWith(blog2.getId())
				.contains(contact0From1));
		blogManager1.removeBlog(blog2);
		sync1To0(1, true);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(2, blogManager1.getBlogs().size());
		assertFalse(blogSharingManager0.getSharedWith(blog2.getId())
				.contains(contact1From0));
		assertEquals(0,
				blogSharingManager1.getSharedWith(blog2.getId()).size());
		assertEquals(SHAREABLE, blogSharingManager0.getSharingStatus(
				blog2.getId(), contact1From0));
	}
	@Test
	public void testInvitationForExistingBlog() throws Exception {
		listenToEvents(true);
		addContacts1And2();
		assertEquals(3, blogManager1.getBlogs().size());
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		Collection<Contact> contacts =
				blogSharingManager1.getSharedWith(blog2.getId());
		assertEquals(2, contacts.size());
		assertTrue(contacts.contains(contact0From1));
		Collection<ConversationMessageHeader> messages =
				db1.transactionWithResult(true, txn -> blogSharingManager1
						.getMessageHeaders(txn, contactId0From1));
		assertEquals(2, messages.size());
		assertEquals(blog2, blogManager1.getBlog(blog2.getId()));
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertEquals(0, blogSharingManager0.getInvitations().size());
		assertEquals(3, blogManager1.getBlogs().size());
	}
	@Test
	public void testRemovingSharedBlog() throws Exception {
		listenToEvents(true);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		assertEquals(3, blogManager1.getBlogs().size());
		Collection<Contact> sharedWith =
				blogSharingManager0.getSharedWith(blog2.getId());
		assertEquals(2, sharedWith.size());
		assertTrue(sharedWith.contains(contact1From0));
		assertTrue(sharedWith.contains(contact2From0));
		Collection<Contact> sharedBy =
				blogSharingManager1.getSharedWith(blog2.getId());
		assertEquals(1, sharedBy.size());
		assertEquals(contact0From1, sharedBy.iterator().next());
		assertTrue(blogManager1.canBeRemoved(blog2));
		blogManager1.removeBlog(blog2);
		sync1To0(1, true);
		sharedWith =
				blogSharingManager0.getSharedWith(blog2.getId());
		assertEquals(1, sharedWith.size());
		assertTrue(sharedWith.contains(contact2From0));
	}
	@Test
	public void testRemovePreSharedBlog() throws Exception {
		listenToEvents(true);
		assertTrue(blogSharingManager0.getSharedWith(blog1.getId())
				.contains(contact1From0));
		assertTrue(blogSharingManager1.getSharedWith(blog1.getId())
				.contains(contact0From1));
		assertTrue(blogManager0.getBlogs().contains(blog1));
		blogManager0.removeBlog(blog1);
		assertFalse(blogManager0.getBlogs().contains(blog1));
		sync0To1(1, true);
		assertFalse(blogSharingManager0.getSharedWith(blog1.getId())
				.contains(contact1From0));
		assertFalse(blogSharingManager1.getSharedWith(blog1.getId())
				.contains(contact0From1));
		assertEquals(SHAREABLE, blogSharingManager1.getSharingStatus(
				blog1.getId(), contact0From1));
	}
	@Test
	public void testSharerIsInformedWhenBlogIsRemovedDueToContactDeletion()
			throws Exception {
		listenToEvents(true);
		blogSharingManager0
				.sendInvitation(blog2.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener1.requestReceived);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(listener0.responseReceived);
		addContacts1And2();
		assertEquals(3, blogManager1.getBlogs().size());
		Collection<Contact> contacts =
				blogSharingManager1.getSharedWith(blog2.getId());
		assertEquals(2, contacts.size());
		assertTrue(contacts.contains(contact0From1));
		contacts = blogSharingManager0.getSharedWith(blog2.getId());
		assertEquals(2, contacts.size());
		assertTrue(contacts.contains(contact1From0));
		assertNotNull(contactId2From1);
		contactManager1.removeContact(contactId2From1);
		sync1To0(1, true);
		contacts = blogSharingManager0.getSharedWith(blog2.getId());
		assertEquals(1, contacts.size());
		assertFalse(contacts.contains(contact1From0));
		try {
			blogManager1.getBlog(blog2.getId());
			fail();
		} catch (NoSuchGroupException e) {
		}
		assertEquals(SHAREABLE, blogSharingManager0.getSharingStatus(
				blog2.getId(), contact1From0));
	}
	@NotNullByDefault
	private class SharerListener implements EventListener {
		private volatile boolean responseReceived = false;
		@Override
		public void eventOccurred(Event e) {
			if (e instanceof BlogInvitationResponseReceivedEvent) {
				BlogInvitationResponseReceivedEvent event =
						(BlogInvitationResponseReceivedEvent) e;
				eventWaiter.assertEquals(contactId1From0, event.getContactId());
				responseReceived = true;
				eventWaiter.resume();
			}
			else if (e instanceof BlogInvitationRequestReceivedEvent) {
				BlogInvitationRequestReceivedEvent event =
						(BlogInvitationRequestReceivedEvent) e;
				eventWaiter.assertEquals(contactId1From0, event.getContactId());
				Blog b = event.getMessageHeader().getNameable();
				try {
					Contact c = contactManager0.getContact(contactId1From0);
					blogSharingManager0.respondToInvitation(b, c, true);
				} catch (DbException ex) {
					eventWaiter.rethrow(ex);
				} finally {
					eventWaiter.resume();
				}
			}
		}
	}
	@NotNullByDefault
	private class InviteeListener implements EventListener {
		private volatile boolean requestReceived = false;
		private final boolean accept, answer;
		private InviteeListener(boolean accept, boolean answer) {
			this.accept = accept;
			this.answer = answer;
		}
		private InviteeListener(boolean accept) {
			this(accept, true);
		}
		@Override
		public void eventOccurred(Event e) {
			if (e instanceof BlogInvitationRequestReceivedEvent) {
				BlogInvitationRequestReceivedEvent event =
						(BlogInvitationRequestReceivedEvent) e;
				requestReceived = true;
				if (!answer) return;
				Blog b = event.getMessageHeader().getNameable();
				try {
					eventWaiter.assertEquals(1,
							blogSharingManager1.getInvitations().size());
					Contact c =
							contactManager1.getContact(event.getContactId());
					blogSharingManager1.respondToInvitation(b, c, accept);
				} catch (DbException ex) {
					eventWaiter.rethrow(ex);
				} finally {
					eventWaiter.resume();
				}
			}
			else if (e instanceof BlogInvitationResponseReceivedEvent) {
				BlogInvitationResponseReceivedEvent event =
						(BlogInvitationResponseReceivedEvent) e;
				eventWaiter.assertEquals(contactId0From1, event.getContactId());
				eventWaiter.resume();
			}
		}
	}
	private void listenToEvents(boolean accept) {
		listener0 = new SharerListener();
		c0.getEventBus().addListener(listener0);
		listener1 = new InviteeListener(accept);
		c1.getEventBus().addListener(listener1);
		SharerListener listener2 = new SharerListener();
		c2.getEventBus().addListener(listener2);
	}
	private Collection<ConversationMessageHeader> getMessages1From0()
			throws DbException {
		return db0.transactionWithResult(true, txn ->
				blogSharingManager0.getMessageHeaders(txn, contactId1From0));
	}
	private Collection<ConversationMessageHeader> getMessages0From1()
			throws DbException {
		return db1.transactionWithResult(true, txn ->
				blogSharingManager1.getMessageHeaders(txn, contactId0From1));
	}
}