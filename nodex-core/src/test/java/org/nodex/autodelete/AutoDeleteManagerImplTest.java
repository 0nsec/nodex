package org.nodex.autodelete;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.db.CommitAction;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.EventAction;
import org.nodex.api.db.Transaction;
import org.nodex.api.event.Event;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.core.test.BrambleMockTestCase;
import org.nodex.api.autodelete.event.AutoDeleteTimerMirroredEvent;
import org.jmock.Expectations;
import org.junit.Test;
import static java.util.Collections.singletonList;
import static org.nodex.core.api.client.ContactGroupConstants.GROUP_KEY_CONTACT_ID;
import static org.nodex.core.test.TestUtils.getContact;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.autodelete.AutoDeleteManager.CLIENT_ID;
import static org.nodex.api.autodelete.AutoDeleteManager.MAJOR_VERSION;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_PREVIOUS_TIMER;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_TIMER;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_TIMESTAMP;
import static org.nodex.autodelete.AutoDeleteConstants.NO_PREVIOUS_TIMER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
@SuppressWarnings("UnnecessaryLocalVariable")
public class AutoDeleteManagerImplTest extends BrambleMockTestCase {
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final GroupFactory groupFactory = context.mock(GroupFactory.class);
	private final ContactGroupFactory contactGroupFactory =
			context.mock(ContactGroupFactory.class);
	private final Group localGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final Group contactGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final Contact contact = getContact();
	private final long now = System.currentTimeMillis();
	private final AutoDeleteManagerImpl autoDeleteManager;
	public AutoDeleteManagerImplTest() {
		context.checking(new Expectations() {{
			oneOf(contactGroupFactory)
					.createLocalGroup(CLIENT_ID.toString(), MAJOR_VERSION);
			will(returnValue(localGroup));
		}});
		autoDeleteManager = new AutoDeleteManagerImpl(db, clientHelper,
				groupFactory, contactGroupFactory);
		context.assertIsSatisfied();
	}
	@Test
	public void testDoesNotAddContactGroupsAtStartupIfLocalGroupExists()
			throws Exception {
		Transaction txn = new Transaction(null, false);
		context.checking(new Expectations() {{
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(true));
		}});
		autoDeleteManager.onDatabaseOpened(txn);
	}
	@Test
	public void testAddsContactGroupsAtStartupIfLocalGroupDoesNotExist()
			throws Exception {
		Transaction txn = new Transaction(null, false);
		context.checking(new Expectations() {{
			oneOf(db).containsGroup(txn, localGroup.getId());
			will(returnValue(false));
			oneOf(db).addGroup(txn, localGroup);
			oneOf(db).getContacts(txn);
			will(returnValue(singletonList(contact)));
		}});
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(db).addGroup(txn, contactGroup);
			oneOf(clientHelper).setContactId(txn, contactGroup.getId(),
					contact.getId());
		}});
		autoDeleteManager.onDatabaseOpened(txn);
	}
	@Test
	public void testAddsContactGroupWhenContactIsAdded() throws Exception {
		Transaction txn = new Transaction(null, false);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(db).addGroup(txn, contactGroup);
			oneOf(clientHelper).setContactId(txn, contactGroup.getId(),
					contact.getId());
		}});
		autoDeleteManager.addingContact(txn, contact);
	}
	@Test
	public void testRemovesContactGroupWhenContactIsRemoved() throws Exception {
		Transaction txn = new Transaction(null, false);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(db).removeGroup(txn, contactGroup);
		}});
		autoDeleteManager.removingContact(txn, contact);
	}
	@Test
	public void testStoresTimer() throws Exception {
		Transaction txn = new Transaction(null, false);
		long oldTimer = MIN_AUTO_DELETE_TIMER_MS;
		long newTimer = MAX_AUTO_DELETE_TIMER_MS;
		BdfDictionary oldMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, oldTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, newTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, oldTimer));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(oldMeta));
			oneOf(clientHelper).mergeGroupMetadata(txn,
					contactGroup.getId(), newMeta);
		}});
		autoDeleteManager.setAutoDeleteTimer(txn, contact.getId(), newTimer);
	}
	@Test
	public void testDoesNotStoreTimerIfUnchanged() throws Exception {
		Transaction txn = new Transaction(null, false);
		long timer = MAX_AUTO_DELETE_TIMER_MS;
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, timer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(meta));
		}});
		autoDeleteManager.setAutoDeleteTimer(txn, contact.getId(), timer);
	}
	@Test
	public void testRetrievesTimer() throws Exception {
		Transaction txn = new Transaction(null, false);
		long timer = MAX_AUTO_DELETE_TIMER_MS;
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_CONTACT_ID, contact.getId().getInt()),
				BdfEntry.of(GROUP_KEY_TIMER, timer));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(meta));
			oneOf(clientHelper).mergeGroupMetadata(txn, contactGroup.getId(),
					newMeta);
		}});
		assertEquals(timer, autoDeleteManager
				.getAutoDeleteTimer(txn, contact.getId(), now));
	}
	@Test
	public void testReturnsConstantIfNoTimerIsStored() throws Exception {
		Transaction txn = new Transaction(null, false);
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_CONTACT_ID, contact.getId().getInt()));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(meta));
			oneOf(clientHelper).mergeGroupMetadata(txn, contactGroup.getId(),
					newMeta);
		}});
		assertEquals(NO_AUTO_DELETE_TIMER, autoDeleteManager
				.getAutoDeleteTimer(txn, contact.getId(), now));
	}
	@Test
	public void testIgnoresReceivedTimerWithEarlierTimestamp()
			throws Exception {
		testIgnoresReceivedTimerWithTimestamp(now - 1);
	}
	@Test
	public void testIgnoresReceivedTimerWithEqualTimestamp() throws Exception {
		testIgnoresReceivedTimerWithTimestamp(now);
	}
	private void testIgnoresReceivedTimerWithTimestamp(long remoteTimestamp)
			throws Exception {
		Transaction txn = new Transaction(null, false);
		long localTimer = MIN_AUTO_DELETE_TIMER_MS;
		long remoteTimer = MAX_AUTO_DELETE_TIMER_MS;
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, localTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(meta));
		}});
		autoDeleteManager.receiveAutoDeleteTimer(txn, contact.getId(),
				remoteTimer, remoteTimestamp);
		assertTrue(txn.getActions().isEmpty());
	}
	@Test
	public void testMirrorsRemoteTimestampIfNoUnsentChange() throws Exception {
		Transaction txn = new Transaction(null, false);
		long localTimer = MIN_AUTO_DELETE_TIMER_MS;
		long remoteTimer = MAX_AUTO_DELETE_TIMER_MS;
		long remoteTimestamp = now + 1;
		BdfDictionary oldMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, localTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMESTAMP, remoteTimestamp),
				BdfEntry.of(GROUP_KEY_TIMER, remoteTimer));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(oldMeta));
			oneOf(clientHelper).mergeGroupMetadata(txn,
					contactGroup.getId(), newMeta);
		}});
		autoDeleteManager.receiveAutoDeleteTimer(txn, contact.getId(),
				remoteTimer, remoteTimestamp);
		assertEvent(txn, remoteTimer);
	}
	@Test
	public void testDoesNotMirrorUnchangedRemoteTimestampIfUnsentChange()
			throws Exception {
		Transaction txn = new Transaction(null, false);
		long localTimer = MIN_AUTO_DELETE_TIMER_MS;
		long remoteTimer = MAX_AUTO_DELETE_TIMER_MS;
		long remoteTimestamp = now + 1;
		BdfDictionary oldMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, localTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, remoteTimer),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMESTAMP, remoteTimestamp));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(oldMeta));
			oneOf(clientHelper).mergeGroupMetadata(txn,
					contactGroup.getId(), newMeta);
		}});
		autoDeleteManager.receiveAutoDeleteTimer(txn, contact.getId(),
				remoteTimer, remoteTimestamp);
		assertTrue(txn.getActions().isEmpty());
	}
	@Test
	public void testMirrorsChangedRemoteTimestampIfUnsentChange()
			throws Exception {
		Transaction txn = new Transaction(null, false);
		long localTimer = MIN_AUTO_DELETE_TIMER_MS;
		long oldRemoteTimer = MAX_AUTO_DELETE_TIMER_MS;
		long newRemoteTimer = MAX_AUTO_DELETE_TIMER_MS - 1;
		long remoteTimestamp = now + 1;
		BdfDictionary oldMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMER, localTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, oldRemoteTimer),
				BdfEntry.of(GROUP_KEY_TIMESTAMP, now));
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_TIMESTAMP, remoteTimestamp),
				BdfEntry.of(GROUP_KEY_TIMER, newRemoteTimer),
				BdfEntry.of(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER));
		expectGetContact(txn);
		expectGetContactGroup();
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn,
					contactGroup.getId());
			will(returnValue(oldMeta));
			oneOf(clientHelper).mergeGroupMetadata(txn,
					contactGroup.getId(), newMeta);
		}});
		autoDeleteManager.receiveAutoDeleteTimer(txn, contact.getId(),
				newRemoteTimer, remoteTimestamp);
		assertEvent(txn, newRemoteTimer);
	}
	private void expectGetContact(Transaction txn) throws Exception {
		context.checking(new Expectations() {{
			oneOf(db).getContact(txn, contact.getId());
			will(returnValue(contact));
		}});
	}
	private void expectGetContactGroup() {
		context.checking(new Expectations() {{
			oneOf(groupFactory).createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
					contact.getAuthor().getId().getBytes());
			will(returnValue(contactGroup));
		}});
	}
	private void assertEvent(Transaction txn, long timer) {
		assertEquals(1, txn.getActions().size());
		CommitAction action = txn.getActions().get(0);
		assertTrue(action instanceof EventAction);
		Event event = ((EventAction) action).getEvent();
		assertTrue(event instanceof AutoDeleteTimerMirroredEvent);
		AutoDeleteTimerMirroredEvent e = (AutoDeleteTimerMirroredEvent) event;
		assertEquals(contact.getId(), e.getContactId());
		assertEquals(timer, e.getNewTimer());
	}
}
