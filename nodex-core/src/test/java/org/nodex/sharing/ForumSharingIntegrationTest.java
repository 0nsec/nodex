package org.nodex.sharing;
import net.jodah.concurrentunit.Waiter;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.sync.Group;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.api.conversation.DeletionResult;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumInvitationRequest;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumPost;
import org.nodex.api.forum.ForumPostHeader;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationRequestReceivedEvent;
import org.nodex.api.forum.event.ForumInvitationResponseReceivedEvent;
import org.nodex.api.sharing.SharingInvitationItem;
import org.nodex.test.NodexIntegrationTest;
import org.nodex.test.NodexIntegrationTestComponent;
import org.nodex.test.DaggerBriarIntegrationTestComponent;
import org.nodex.nullsafety.NotNullByDefault;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import static java.util.Collections.emptySet;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.forum.ForumSharingManager.CLIENT_ID;
import static org.nodex.api.forum.ForumSharingManager.MAJOR_VERSION;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHAREABLE;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHARING;
import static org.nodex.test.NodexTestUtils.assertGroupCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
public class ForumSharingIntegrationTest
		extends NodexIntegrationTest<NodexIntegrationTestComponent> {
	private ForumManager forumManager0, forumManager1;
	private MessageEncoder messageEncoder;
	private Listener listener0, listener2, listener1;
	private Forum forum;
	private volatile ForumSharingManager forumSharingManager0;
	private volatile ForumSharingManager forumSharingManager1;
	private volatile ForumSharingManager forumSharingManager2;
	private volatile Waiter eventWaiter;
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		forumManager0 = c0.getForumManager();
		forumManager1 = c1.getForumManager();
		forumSharingManager0 = c0.getForumSharingManager();
		forumSharingManager1 = c1.getForumSharingManager();
		forumSharingManager2 = c2.getForumSharingManager();
		messageEncoder = new MessageEncoderImpl(clientHelper, messageFactory);
		eventWaiter = new Waiter();
		listener0 = new Listener();
		c0.getEventBus().addListener(listener0);
		listener1 = new Listener();
		c1.getEventBus().addListener(listener1);
		listener2 = new Listener();
		c2.getEventBus().addListener(listener2);
		addContacts1And2();
		addForumForSharer();
	}
	@Override
	protected void createComponents() {
		NodexIntegrationTestComponent component =
				DaggerBriarIntegrationTestComponent.builder().build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);
		c0 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c0);
		c1 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c1);
		c2 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.build();
		NodexIntegrationTestComponent.Helper.injectEagerSingletons(c2);
	}
	private void addForumForSharer() throws DbException {
		forum = forumManager0.addForum("Test Forum");
	}
	@Test
	public void testSuccessfulSharing() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		Collection<ConversationMessageHeader> messages = getMessages1From0();
		assertEquals(1, messages.size());
		assertMessageState(messages.iterator().next(), true, false, false);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		messages = getMessages0From1();
		assertEquals(2, messages.size());
		for (ConversationMessageHeader h : messages) {
			if (h instanceof ConversationResponse) {
				assertMessageState(h, true, false, false);
			}
		}
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(1, forumManager1.getForums().size());
		Collection<ConversationMessageHeader> list = getMessages0From1();
		assertEquals(2, list.size());
		for (ConversationMessageHeader m : list) {
			if (m instanceof ForumInvitationRequest) {
				ForumInvitationRequest invitation = (ForumInvitationRequest) m;
				assertTrue(invitation.wasAnswered());
				assertEquals(forum.getName(), invitation.getName());
				assertEquals(forum, invitation.getNameable());
				assertEquals("Hi!", invitation.getText());
				assertTrue(invitation.canBeOpened());
			} else {
				ForumInvitationResponse response = (ForumInvitationResponse) m;
				assertEquals(forum.getId(), response.getShareableId());
				assertTrue(response.wasAccepted());
				assertTrue(response.isLocal());
			}
		}
		assertEquals(2, getMessages1From0().size());
		Contact c1 = contactManager0.getContact(contactId1From0);
		assertEquals(SHARING,
				forumSharingManager0.getSharingStatus(forum.getId(), c1));
		Contact c0 = contactManager1.getContact(contactId0From1);
		assertEquals(SHARING,
				forumSharingManager1.getSharingStatus(forum.getId(), c0));
	}
	@Test
	public void testSuccessfulSharingWithAutoDelete() throws Exception {
		setAutoDeleteTimer(c0, contactId1From0, MIN_AUTO_DELETE_TIMER_MS);
		setAutoDeleteTimer(c1, contactId0From1, MIN_AUTO_DELETE_TIMER_MS);
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(1, forumManager1.getForums().size());
		for (ConversationMessageHeader h : getMessages1From0()) {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		}
		for (ConversationMessageHeader h : getMessages0From1()) {
			assertEquals(MIN_AUTO_DELETE_TIMER_MS, h.getAutoDeleteTimer());
		}
	}
	@Test
	public void testDeclinedSharing() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, false);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, false);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(0, forumManager1.getForums().size());
		assertEquals(0, forumSharingManager1.getInvitations().size());
		Collection<ConversationMessageHeader> list = getMessages0From1();
		assertEquals(2, list.size());
		for (ConversationMessageHeader m : list) {
			if (m instanceof ForumInvitationRequest) {
				ForumInvitationRequest invitation = (ForumInvitationRequest) m;
				assertEquals(forum, invitation.getNameable());
				assertTrue(invitation.wasAnswered());
				assertEquals(forum.getName(), invitation.getName());
				assertNull(invitation.getText());
				assertFalse(invitation.canBeOpened());
			} else {
				ForumInvitationResponse response = (ForumInvitationResponse) m;
				assertEquals(forum.getId(), response.getShareableId());
				assertFalse(response.wasAccepted());
				assertTrue(response.isLocal());
			}
		}
		assertEquals(2, getMessages1From0().size());
		Contact c1 = contactManager0.getContact(contactId1From0);
		assertEquals(SHAREABLE,
				forumSharingManager0.getSharingStatus(forum.getId(), c1));
		forumManager0.removeForum(forum);
		db0.transaction(false, txn -> forumManager0.addForum(txn, forum));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		listener1.requestReceived = false;
		listener1.requestContactId = null;
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
	}
	@Test
	public void testInviteeLeavesAfterFinished() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(1, forumManager1.getForums().size());
		assertTrue(forumManager1.getForums().contains(forum));
		assertTrue(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		Contact contact0 = contactManager1.getContact(contactId1From0);
		assertTrue(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0));
		forumManager1.removeForum(forum);
		sync1To0(1, true);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(0, forumManager1.getForums().size());
		assertFalse(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		assertFalse(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0));
		assertEquals(SHAREABLE, forumSharingManager0
				.getSharingStatus(forum.getId(), contact1From0));
		try {
			forumSharingManager1.getSharingStatus(forum.getId(), contact0From1);
			fail();
		} catch (ProtocolStateException e) {
		}
		sync0To1(1, true);
		assertEquals(SHAREABLE, forumSharingManager1
				.getSharingStatus(forum.getId(), contact0From1));
	}
	@Test
	public void testSharerLeavesAfterFinished() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(0, forumSharingManager0.getInvitations().size());
		assertEquals(1, forumManager1.getForums().size());
		assertTrue(forumManager1.getForums().contains(forum));
		Contact c1 = contactManager0.getContact(contactId1From0);
		assertTrue(forumSharingManager0.getSharedWith(forum.getId())
				.contains(c1));
		Contact contact0 = contactManager1.getContact(contactId1From0);
		assertTrue(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0));
		forumManager0.removeForum(forum);
		sync0To1(1, true);
		assertEquals(0, forumManager0.getForums().size());
		assertEquals(1, forumManager1.getForums().size());
		Contact c0 = contactManager1.getContact(contactId0From1);
		assertFalse(forumSharingManager1.getSharedWith(forum.getId())
				.contains(c0));
		assertFalse(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0));
		assertEquals(SHAREABLE,
				forumSharingManager1.getSharingStatus(forum.getId(), c0));
		sync1To0(1, true);
		assertEquals(SHAREABLE,
				forumSharingManager0.getSharingStatus(forum.getId(), c1));
		forumManager1.removeForum(forum);
		db0.transaction(false, txn -> forumManager0.addForum(txn, forum));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		listener1.requestReceived = false;
		listener1.requestContactId = null;
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
	}
	@Test
	public void testSharerLeavesBeforeResponse() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		forumManager0.removeForum(forum);
		sync0To1(2, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		assertEquals(0, forumSharingManager1.getInvitations().size());
		assertEquals(0, forumManager1.getForums().size());
		addForumForSharer();
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		forumManager0.removeForum(forum);
		sync0To1(1, true);
		assertEquals(0, forumSharingManager1.getInvitations().size());
		assertEquals(1, forumManager1.getForums().size());
	}
	@Test
	public void testSharingSameForumWithEachOther() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		Group group = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, contact0From1);
		assertEquals(2, c1.getMessageTracker().getGroupCount(group.getId())
				.getMsgCount());
		assertEquals(1, forumManager1.getForums().size());
		forumSharingManager1
				.sendInvitation(forum.getId(), contactId0From1,
						"I am re-sharing this forum with you.");
		assertEquals(2, c1.getMessageTracker().getGroupCount(group.getId())
				.getMsgCount());
	}
	@Test
	public void testSharingSameForumWithEachOtherBeforeAccept()
			throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		assertEquals(1, forumSharingManager1.getInvitations().size());
		Group group = contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, contact0From1);
		assertEquals(1, c1.getMessageTracker().getGroupCount(group.getId())
				.getMsgCount());
		forumSharingManager1
				.sendInvitation(forum.getId(), contactId0From1,
						"I am re-sharing this forum with you.");
		assertEquals(1, c1.getMessageTracker().getGroupCount(group.getId())
				.getMsgCount());
	}
	@Test
	public void testSharingSameForumWithEachOtherAtSameTime() throws Exception {
		db1.transaction(false, txn -> forumManager1.addForum(txn, forum));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		forumSharingManager1
				.sendInvitation(forum.getId(), contactId0From1,
						"I am re-sharing this forum with you.");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener0, contactId1From0);
		assertTrue(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		assertTrue(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0From1));
		assertEquals(2, getMessages1From0().size());
		assertEquals(2, getMessages0From1().size());
		assertTrue(forumSharingManager0.getInvitations().isEmpty());
		assertTrue(forumSharingManager1.getInvitations().isEmpty());
	}
	@Test
	public void testContactRemoved() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(1, forumManager1.getForums().size());
		assertEquals(1,
				forumSharingManager0.getSharedWith(forum.getId()).size());
		removeAllContacts();
		assertEquals(1, forumManager1.getForums().size());
		assertEquals(0,
				forumSharingManager0.getSharedWith(forum.getId()).size());
		addDefaultContacts();
		addContacts1And2();
		assertEquals(SHAREABLE, forumSharingManager0
				.getSharingStatus(forum.getId(), contact1From0));
		assertEquals(SHAREABLE, forumSharingManager0
				.getSharingStatus(forum.getId(), contact2From0));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertEquals(1, forumManager1.getForums().size());
		assertEquals(1,
				forumSharingManager0.getSharedWith(forum.getId()).size());
	}
	@Test
	public void testTwoContactsShareSameForum() throws Exception {
		db2.transaction(false, txn -> db2.addGroup(txn, forum.getGroup()));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		assertNotNull(contactId1From2);
		forumSharingManager2
				.sendInvitation(forum.getId(), contactId1From2, null);
		sync2To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId2From1);
		Collection<SharingInvitationItem> forums =
				forumSharingManager1.getInvitations();
		assertEquals(1, forums.size());
		assertEquals(2, forums.iterator().next().getNewSharers().size());
		assertEquals(forum, forums.iterator().next().getShareable());
		assertNotNull(contactId2From1);
		Contact contact2From1 = contactManager1.getContact(contactId2From1);
		forumSharingManager1.respondToInvitation(forum, contact2From1, true);
		sync1To2(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener2, contactId1From2, true);
		Contact contact0From1 = contactManager1.getContact(contactId0From1);
		forumSharingManager1.respondToInvitation(forum, contact0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		Collection<Contact> contacts =
				forumSharingManager1.getSharedWith(forum.getId());
		assertEquals(2, contacts.size());
	}
	@Test
	public void testSyncAfterReSharing() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		long time = c0.getClock().currentTimeMillis();
		String text = getRandomString(42);
		ForumPost p = forumPostFactory
				.createPost(forum.getId(), time, null, author0, text);
		forumManager0.addLocalPost(p);
		sync0To1(1, true);
		Collection<ForumPostHeader> headers =
				forumManager1.getPostHeaders(forum.getId());
		assertEquals(1, headers.size());
		ForumPostHeader header = headers.iterator().next();
		assertEquals(p.getMessage().getId(), header.getId());
		assertEquals(author0, header.getAuthor());
		time = c1.getClock().currentTimeMillis();
		text = getRandomString(42);
		p = forumPostFactory
				.createPost(forum.getId(), time, null, author1, text);
		forumManager1.addLocalPost(p);
		sync1To0(1, true);
		headers = forumManager1.getPostHeaders(forum.getId());
		assertEquals(2, headers.size());
		boolean found = false;
		for (ForumPostHeader h : headers) {
			if (p.getMessage().getId().equals(h.getId())) {
				found = true;
				assertEquals(author1, h.getAuthor());
			}
		}
		assertTrue(found);
		removeAllContacts();
		addDefaultContacts();
		addContacts1And2();
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		syncMessage(c1, c0, contactId0From1, 1, 2, 0, 1);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		time = c1.getClock().currentTimeMillis();
		text = getRandomString(42);
		p = forumPostFactory
				.createPost(forum.getId(), time, null, author1, text);
		forumManager1.addLocalPost(p);
		sync1To0(1, true);
		headers = forumManager1.getPostHeaders(forum.getId());
		assertEquals(3, headers.size());
		found = false;
		for (ForumPostHeader h : headers) {
			if (p.getMessage().getId().equals(h.getId())) {
				found = true;
				assertEquals(author1, h.getAuthor());
			}
		}
		assertTrue(found);
	}
	@Test
	public void testSessionResetAfterAbort() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, "Hi!");
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		MessageId invitationId = null;
		for (ConversationMessageHeader m : getMessages0From1()) {
			if (m instanceof ForumInvitationRequest) {
				invitationId = m.getId();
			}
		}
		assertNotNull(invitationId);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertTrue(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		assertTrue(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0From1));
		Message m = messageEncoder.encodeAcceptMessage(
				forumSharingManager0.getContactGroup(contact1From0).getId(),
				forum.getId(), c0.getClock().currentTimeMillis(), invitationId);
		c0.getClientHelper().addLocalMessage(m, new BdfDictionary(), true);
		sync0To1(1, true);
		sync1To0(1, true);
		assertFalse(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		assertFalse(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0From1));
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertRequestReceived(listener1, contactId0From1);
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertResponseReceived(listener0, contactId1From0, true);
		assertTrue(forumSharingManager0.getSharedWith(forum.getId())
				.contains(contact1From0));
		assertTrue(forumSharingManager1.getSharedWith(forum.getId())
				.contains(contact0From1));
	}
	@Test
	public void testDeletingAllMessagesWhenCompletingSession()
			throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertFalse(deleteAllMessages1From0().allDeleted());
		assertTrue(deleteAllMessages1From0().hasInvitationSessionInProgress());
		assertFalse(deleteAllMessages0From1().allDeleted());
		assertTrue(deleteAllMessages0From1().hasInvitationSessionInProgress());
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		GroupId g1From0 =
				forumSharingManager0.getContactGroup(contact1From0).getId();
		GroupId g0From1 =
				forumSharingManager1.getContactGroup(contact0From1).getId();
		assertGroupCount(messageTracker0, g1From0, 2, 1);
		assertGroupCount(messageTracker1, g0From1, 2, 1);
		assertTrue(deleteAllMessages1From0().allDeleted());
		assertEquals(0, getMessages1From0().size());
		assertGroupCount(messageTracker0, g1From0, 0, 0);
		assertFalse(deleteAllMessages0From1().allDeleted());
		assertTrue(deleteAllMessages0From1().hasInvitationSessionInProgress());
		assertGroupCount(messageTracker1, g0From1, 2, 1);
		ack0To1(1);
		assertTrue(deleteAllMessages0From1().allDeleted());
		assertEquals(0, getMessages0From1().size());
		assertGroupCount(messageTracker1, g0From1, 0, 0);
		forumManager0.removeForum(forum);
		sync0To1(1, true);
		sync1To0(1, true);
		forumSharingManager1
				.sendInvitation(forum.getId(), contactId0From1, null);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertFalse(deleteAllMessages1From0().allDeleted());
		assertTrue(deleteAllMessages1From0().hasInvitationSessionInProgress());
		assertEquals(1, getMessages1From0().size());
		assertGroupCount(messageTracker0, g1From0, 1, 1);
		assertFalse(deleteAllMessages0From1().allDeleted());
		assertTrue(deleteAllMessages0From1().hasInvitationSessionInProgress());
		assertEquals(1, getMessages0From1().size());
		assertGroupCount(messageTracker1, g0From1, 1, 0);
		forumSharingManager0.respondToInvitation(forum, contact1From0, true);
		sync0To1(1, true);
		ack1To0(1);
		assertTrue(deleteAllMessages1From0().allDeleted());
		assertEquals(0, getMessages1From0().size());
		assertGroupCount(messageTracker0, g1From0, 0, 0);
		assertTrue(deleteAllMessages0From1().allDeleted());
		assertEquals(0, getMessages0From1().size());
		assertGroupCount(messageTracker1, g0From1, 0, 0);
	}
	@Test
	public void testDeletingAllMessagesAfterDecline()
			throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		respondToRequest(contactId0From1, false);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertTrue(deleteAllMessages1From0().allDeleted());
		assertEquals(0, getMessages1From0().size());
		assertFalse(deleteAllMessages0From1().allDeleted());
		ack0To1(1);
		assertTrue(deleteAllMessages0From1().allDeleted());
		assertEquals(0, getMessages0From1().size());
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertFalse(deleteAllMessages1From0().allDeleted());
		assertEquals(1, getMessages1From0().size());
		assertFalse(deleteAllMessages0From1().allDeleted());
		assertEquals(1, getMessages0From1().size());
	}
	@Test
	public void testDeletingSomeMessages() throws Exception {
		forumSharingManager0
				.sendInvitation(forum.getId(), contactId1From0, null);
		sync0To1(1, true);
		eventWaiter.await(TIMEOUT, 1);
		Collection<ConversationMessageHeader> m0 = getMessages1From0();
		assertEquals(1, m0.size());
		MessageId messageId = m0.iterator().next().getId();
		Set<MessageId> toDelete = new HashSet<>();
		toDelete.add(messageId);
		assertFalse(deleteMessages1From0(toDelete).allDeleted());
		assertTrue(deleteMessages1From0(toDelete)
				.hasInvitationSessionInProgress());
		respondToRequest(contactId0From1, true);
		sync1To0(1, true);
		eventWaiter.await(TIMEOUT, 1);
		assertFalse(deleteMessages1From0(toDelete).allDeleted());
		assertTrue(
				deleteMessages1From0(toDelete).hasNotAllInvitationSelected());
		assertFalse(deleteMessages0From1(toDelete).allDeleted());
		assertTrue(
				deleteMessages0From1(toDelete).hasNotAllInvitationSelected());
		m0 = getMessages1From0();
		assertEquals(2, m0.size());
		for (ConversationMessageHeader h : m0) {
			if (!h.getId().equals(messageId)) toDelete.add(h.getId());
		}
		assertTrue(deleteMessages1From0(toDelete).allDeleted());
		assertEquals(0, getMessages1From0().size());
		assertTrue(deleteMessages1From0(toDelete).allDeleted());
		assertFalse(deleteMessages0From1(toDelete).allDeleted());
		assertFalse(
				deleteMessages0From1(toDelete).hasNotAllInvitationSelected());
		assertTrue(deleteMessages0From1(toDelete)
				.hasInvitationSessionInProgress());
		ack0To1(1);
		assertTrue(deleteMessages0From1(toDelete).allDeleted());
		assertEquals(0, getMessages0From1().size());
		assertTrue(deleteMessages0From1(toDelete).allDeleted());
	}
	@Test
	public void testDeletingEmptySet() throws Exception {
		assertTrue(deleteMessages0From1(emptySet()).allDeleted());
	}
	private Collection<ConversationMessageHeader> getMessages1From0()
			throws DbException {
		return db0.transactionWithResult(true, txn -> forumSharingManager0
				.getMessageHeaders(txn, contactId1From0));
	}
	private Collection<ConversationMessageHeader> getMessages0From1()
			throws DbException {
		return db1.transactionWithResult(true, txn -> forumSharingManager1
				.getMessageHeaders(txn, contactId0From1));
	}
	private DeletionResult deleteAllMessages1From0() throws DbException {
		return db0.transactionWithResult(false, txn -> forumSharingManager0
				.deleteAllMessages(txn, contactId1From0));
	}
	private DeletionResult deleteAllMessages0From1() throws DbException {
		return db1.transactionWithResult(false, txn -> forumSharingManager1
				.deleteAllMessages(txn, contactId0From1));
	}
	private DeletionResult deleteMessages1From0(Set<MessageId> toDelete)
			throws DbException {
		return db0.transactionWithResult(false, txn -> forumSharingManager0
				.deleteMessages(txn, contactId1From0, toDelete));
	}
	private DeletionResult deleteMessages0From1(Set<MessageId> toDelete)
			throws DbException {
		return db1.transactionWithResult(false, txn -> forumSharingManager1
				.deleteMessages(txn, contactId0From1, toDelete));
	}
	private void respondToRequest(ContactId contactId, boolean accept)
			throws DbException {
		assertEquals(1, forumSharingManager1.getInvitations().size());
		SharingInvitationItem invitation =
				forumSharingManager1.getInvitations().iterator().next();
		assertEquals(forum, invitation.getShareable());
		Contact c = contactManager1.getContact(contactId);
		forumSharingManager1.respondToInvitation(forum, c, accept);
	}
	private void assertRequestReceived(Listener listener, ContactId contactId) {
		assertTrue(listener.requestReceived);
		assertEquals(contactId, listener.requestContactId);
		listener.reset();
	}
	private void assertResponseReceived(Listener listener, ContactId contactId,
			boolean accept) {
		assertTrue(listener.responseReceived);
		assertEquals(contactId, listener.responseContactId);
		assertEquals(accept, listener.responseAccepted);
		listener.reset();
	}
	@NotNullByDefault
	private class Listener implements EventListener {
		private volatile boolean requestReceived = false;
		@Nullable
		private volatile ContactId requestContactId = null;
		private volatile boolean responseReceived = false;
		@Nullable
		private volatile ContactId responseContactId = null;
		private volatile boolean responseAccepted = false;
		@Override
		public void eventOccurred(Event e) {
			if (e instanceof ForumInvitationRequestReceivedEvent) {
				ForumInvitationRequestReceivedEvent event =
						(ForumInvitationRequestReceivedEvent) e;
				requestReceived = true;
				requestContactId = event.getContactId();
				eventWaiter.resume();
			} else if (e instanceof ForumInvitationResponseReceivedEvent) {
				ForumInvitationResponseReceivedEvent event =
						(ForumInvitationResponseReceivedEvent) e;
				responseReceived = true;
				responseContactId = event.getContactId();
				responseAccepted = event.getMessageHeader().wasAccepted();
				eventWaiter.resume();
			}
		}
		private void reset() {
			requestReceived = responseReceived = responseAccepted = false;
			requestContactId = responseContactId = null;
		}
	}
}