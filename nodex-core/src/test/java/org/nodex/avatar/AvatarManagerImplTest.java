package org.nodex.avatar;
import org.nodex.api.FormatException;
import org.nodex.api.Pair;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.EventAction;
import org.nodex.api.db.Metadata;
import org.nodex.api.db.Transaction;
import org.nodex.api.event.Event;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Group.Visibility;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.core.test.BrambleMockTestCase;
import org.nodex.core.test.DbExpectations;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.avatar.AvatarMessageEncoder;
import org.nodex.api.avatar.event.AvatarUpdatedEvent;
import org.jmock.Expectations;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import static org.nodex.api.sync.Group.Visibility.INVISIBLE;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.api.sync.Group.Visibility.VISIBLE;
import static org.nodex.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static org.nodex.core.test.TestUtils.getContact;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getLocalAuthor;
import static org.nodex.core.test.TestUtils.getMessage;
import static org.nodex.core.test.TestUtils.getRandomBytes;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static org.nodex.api.avatar.AvatarManager.CLIENT_ID;
import static org.nodex.api.avatar.AvatarManager.MAJOR_VERSION;
import static org.nodex.avatar.AvatarConstants.GROUP_KEY_CONTACT_ID;
import static org.nodex.avatar.AvatarConstants.MSG_KEY_VERSION;
import static org.junit.Assert.assertEquals;
public class AvatarManagerImplTest extends BrambleMockTestCase {
	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final IdentityManager identityManager =
			context.mock(IdentityManager.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final ClientVersioningManager clientVersioningManager =
			context.mock(ClientVersioningManager.class);
	private final MetadataParser metadataParser =
			context.mock(MetadataParser.class);
	private final GroupFactory groupFactory = context.mock(GroupFactory.class);
	private final AvatarMessageEncoder avatarMessageEncoder =
			context.mock(AvatarMessageEncoder.class);
	private final Group localGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final GroupId localGroupId = localGroup.getId();
	private final LocalAuthor localAuthor = getLocalAuthor();
	private final Contact contact = getContact();
	private final Group contactGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION, 32);
	private final GroupId contactGroupId = contactGroup.getId();
	private final Message ourMsg = getMessage(localGroupId);
	private final Message contactMsg = getMessage(contactGroupId);
	private final Metadata meta = new Metadata();
	private final String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
	private final BdfDictionary metaDict = BdfDictionary.of(
			BdfEntry.of(MSG_KEY_VERSION, 1),
			BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
	);
	private final AvatarManagerImpl avatarManager =
			new AvatarManagerImpl(db, identityManager, clientHelper,
					clientVersioningManager, metadataParser, groupFactory,
					avatarMessageEncoder);
	@Test
	public void testOpenDatabaseHookWhenGroupExists() throws DbException {
		Transaction txn = new Transaction(null, false);
		expectCreateGroup(localAuthor.getId(), localGroup);
		context.checking(new Expectations() {{
			oneOf(identityManager).getLocalAuthor(txn);
			will(returnValue(localAuthor));
			oneOf(db).containsGroup(txn, localGroupId);
			will(returnValue(true));
		}});
		avatarManager.onDatabaseOpened(txn);
	}
	@Test
	public void testOpenDatabaseHook() throws DbException, FormatException {
		Transaction txn = new Transaction(null, false);
		expectCreateGroup(localAuthor.getId(), localGroup);
		context.checking(new Expectations() {{
			oneOf(identityManager).getLocalAuthor(txn);
			will(returnValue(localAuthor));
			oneOf(db).containsGroup(txn, localGroupId);
			will(returnValue(false));
			oneOf(db).addGroup(txn, localGroup);
			oneOf(db).getContacts(txn);
			will(returnValue(Collections.singletonList(contact)));
		}});
		expectAddingContact(txn, contact, SHARED);
		avatarManager.onDatabaseOpened(txn);
	}
	@Test
	public void testAddingContact() throws DbException, FormatException {
		Transaction txn = new Transaction(null, false);
		expectAddingContact(txn, contact, INVISIBLE);
		avatarManager.addingContact(txn, contact);
		Contact contact2 = getContact();
		expectAddingContact(txn, contact2, VISIBLE);
		avatarManager.addingContact(txn, contact2);
	}
	@Test
	public void testRemovingContact() throws DbException {
		Transaction txn = new Transaction(null, false);
		expectCreateGroup(contact.getAuthor().getId(), contactGroup);
		context.checking(new Expectations() {{
			oneOf(db).removeGroup(txn, contactGroup);
		}});
		avatarManager.removingContact(txn, contact);
	}
	@Test
	public void testOnClientVisibilityChangingVisible() throws DbException {
		testOnClientVisibilityChanging(VISIBLE);
	}
	@Test
	public void testOnClientVisibilityChangingShared() throws DbException {
		testOnClientVisibilityChanging(SHARED);
	}
	@Test
	public void testOnClientVisibilityChangingInvisible() throws DbException {
		testOnClientVisibilityChanging(INVISIBLE);
	}
	private void testOnClientVisibilityChanging(Visibility v)
			throws DbException {
		Transaction txn = new Transaction(null, false);
		expectGetOurGroup(txn);
		expectCreateGroup(contact.getAuthor().getId(), contactGroup);
		expectSetGroupVisibility(txn, contact.getId(), localGroupId, v);
		expectSetGroupVisibility(txn, contact.getId(), contactGroupId, v);
		avatarManager.onClientVisibilityChanging(txn, contact, v);
	}
	@Test
	public void testFirstIncomingMessage()
			throws DbException, InvalidMessageException, FormatException {
		Transaction txn = new Transaction(null, false);
		BdfDictionary d = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
		);
		expectGetOurGroup(txn);
		context.checking(new Expectations() {{
			oneOf(metadataParser).parse(meta);
			will(returnValue(d));
		}});
		expectFindLatest(txn, contactGroupId, new MessageId(getRandomId()),
				null);
		expectGetContactId(txn, contactGroupId, contact.getId());
		assertEquals(ACCEPT_DO_NOT_SHARE,
				avatarManager.incomingMessage(txn, contactMsg, meta));
		assertEquals(1, txn.getActions().size());
		Event event = ((EventAction) txn.getActions().get(0)).getEvent();
		AvatarUpdatedEvent avatarUpdatedEvent = (AvatarUpdatedEvent) event;
		assertEquals(contactMsg.getId(),
				avatarUpdatedEvent.getAttachmentHeader().getMessageId());
		assertEquals(contact.getId(), avatarUpdatedEvent.getContactId());
	}
	@Test
	public void testNewerIncomingMessage()
			throws DbException, InvalidMessageException, FormatException {
		Transaction txn = new Transaction(null, false);
		BdfDictionary d = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, 1),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
		);
		MessageId latestMsgId = new MessageId(getRandomId());
		BdfDictionary latest = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, 0),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
		);
		expectGetOurGroup(txn);
		context.checking(new Expectations() {{
			oneOf(metadataParser).parse(meta);
			will(returnValue(d));
			oneOf(db).deleteMessage(txn, latestMsgId);
			oneOf(db).deleteMessageMetadata(txn, latestMsgId);
		}});
		expectFindLatest(txn, contactGroupId, latestMsgId, latest);
		expectGetContactId(txn, contactGroupId, contact.getId());
		assertEquals(ACCEPT_DO_NOT_SHARE,
				avatarManager.incomingMessage(txn, contactMsg, meta));
		assertEquals(1, txn.getActions().size());
	}
	@Test
	public void testDeleteOlderIncomingMessage()
			throws DbException, InvalidMessageException, FormatException {
		Transaction txn = new Transaction(null, false);
		BdfDictionary d = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, 0),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
		);
		MessageId latestMsgId = new MessageId(getRandomId());
		BdfDictionary latest = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, 1),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType)
		);
		expectGetOurGroup(txn);
		context.checking(new Expectations() {{
			oneOf(metadataParser).parse(meta);
			will(returnValue(d));
			oneOf(db).deleteMessage(txn, contactMsg.getId());
			oneOf(db).deleteMessageMetadata(txn, contactMsg.getId());
		}});
		expectFindLatest(txn, contactGroupId, latestMsgId, latest);
		assertEquals(ACCEPT_DO_NOT_SHARE,
				avatarManager.incomingMessage(txn, contactMsg, meta));
		assertEquals(0, txn.getActions().size());
	}
	@Test(expected = InvalidMessageException.class)
	public void testIncomingMessageInOwnGroup()
			throws DbException, InvalidMessageException {
		Transaction txn = new Transaction(null, false);
		expectGetOurGroup(txn);
		assertEquals(ACCEPT_DO_NOT_SHARE,
				avatarManager.incomingMessage(txn, ourMsg, meta));
	}
	@Test
	public void testAddAvatar() throws Exception {
		byte[] avatarBytes = getRandomBytes(42);
		InputStream inputStream = new ByteArrayInputStream(avatarBytes);
		Transaction txn = new Transaction(null, true);
		Transaction txn2 = new Transaction(null, false);
		long latestVersion = metaDict.getLong(MSG_KEY_VERSION);
		long version = latestVersion + 1;
		Message newMsg = getMessage(localGroupId);
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, version),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType),
				BdfEntry.of(MSG_KEY_DESCRIPTOR_LENGTH, 0)
		);
		context.checking(new DbExpectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
			oneOf(avatarMessageEncoder).encodeUpdateMessage(localGroupId,
					version, contentType, inputStream);
			will(returnValue(new Pair<>(newMsg, newMeta)));
			oneOf(db).transactionWithResult(with(false), withDbCallable(txn2));
			oneOf(db).deleteMessage(txn2, ourMsg.getId());
			oneOf(db).deleteMessageMetadata(txn2, ourMsg.getId());
			oneOf(clientHelper)
					.addLocalMessage(txn2, newMsg, newMeta, true, false);
		}});
		expectGetOurGroup(txn);
		expectFindLatest(txn, localGroupId, ourMsg.getId(), metaDict);
		expectFindLatest(txn2, localGroupId, ourMsg.getId(), metaDict);
		AttachmentHeader header =
				avatarManager.addAvatar(contentType, inputStream);
		assertEquals(newMsg.getId(), header.getMessageId());
		assertEquals(contentType, header.getContentType());
	}
	@Test
	public void testAddAvatarConcurrently() throws Exception {
		byte[] avatarBytes = getRandomBytes(42);
		InputStream inputStream = new ByteArrayInputStream(avatarBytes);
		Transaction txn = new Transaction(null, true);
		Transaction txn2 = new Transaction(null, false);
		long latestVersion = metaDict.getLong(MSG_KEY_VERSION);
		Message newMsg = getMessage(localGroupId);
		BdfDictionary newMeta = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, latestVersion + 2),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType),
				BdfEntry.of(MSG_KEY_DESCRIPTOR_LENGTH, 0)
		);
		context.checking(new DbExpectations() {{
			oneOf(db).startTransaction(true);
			will(returnValue(txn));
			oneOf(db).commitTransaction(txn);
			oneOf(db).endTransaction(txn);
			oneOf(avatarMessageEncoder).encodeUpdateMessage(localGroupId,
					latestVersion + 1, contentType, inputStream);
			will(returnValue(new Pair<>(newMsg, newMeta)));
			oneOf(db).transactionWithResult(with(false), withDbCallable(txn2));
		}});
		expectGetOurGroup(txn);
		expectFindLatest(txn, localGroupId, ourMsg.getId(), metaDict);
		expectFindLatest(txn2, localGroupId, ourMsg.getId(), newMeta);
		AttachmentHeader header =
				avatarManager.addAvatar(contentType, inputStream);
		assertEquals(ourMsg.getId(), header.getMessageId());
		assertEquals(contentType, header.getContentType());
	}
	private void expectGetContactId(Transaction txn, GroupId groupId,
			ContactId contactId) throws DbException, FormatException {
		BdfDictionary d = BdfDictionary
				.of(BdfEntry.of(GROUP_KEY_CONTACT_ID, contactId.getInt()));
		context.checking(new Expectations() {{
			oneOf(clientHelper).getGroupMetadataAsDictionary(txn, groupId);
			will(returnValue(d));
		}});
	}
	private void expectFindLatest(Transaction txn, GroupId groupId,
			MessageId messageId, @Nullable BdfDictionary d)
			throws DbException, FormatException {
		Map<MessageId, BdfDictionary> map = new HashMap<>();
		if (d != null) map.put(messageId, d);
		context.checking(new Expectations() {{
			oneOf(clientHelper)
					.getMessageMetadataAsDictionary(txn, groupId);
			will(returnValue(map));
		}});
	}
	private void expectSetGroupVisibility(Transaction txn, ContactId contactId,
			GroupId groupId, Visibility v) throws DbException {
		context.checking(new Expectations() {{
			oneOf(db).setGroupVisibility(txn, contactId, groupId, v);
		}});
	}
	private void expectAddingContact(Transaction txn, Contact c, Visibility v)
			throws DbException, FormatException {
		BdfDictionary groupMeta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_CONTACT_ID, c.getId().getInt())
		);
		expectGetOurGroup(txn);
		context.checking(new Expectations() {{
			oneOf(groupFactory).createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
					c.getAuthor().getId().getBytes());
			will(returnValue(contactGroup));
			oneOf(db).addGroup(txn, contactGroup);
			oneOf(clientHelper)
					.mergeGroupMetadata(txn, contactGroupId, groupMeta);
			oneOf(clientVersioningManager)
					.getClientVisibility(txn, c.getId(), CLIENT_ID,
							MAJOR_VERSION);
			will(returnValue(v));
		}});
		expectSetGroupVisibility(txn, c.getId(), localGroupId, v);
		expectSetGroupVisibility(txn, c.getId(), contactGroupId, v);
	}
	private void expectGetOurGroup(Transaction txn) throws DbException {
		context.checking(new Expectations() {{
			oneOf(identityManager).getLocalAuthor(txn);
			will(returnValue(localAuthor));
		}});
		expectCreateGroup(localAuthor.getId(), localGroup);
	}
	private void expectCreateGroup(AuthorId authorId, Group group) {
		context.checking(new Expectations() {{
			oneOf(groupFactory)
					.createGroup(CLIENT_ID.toString(), MAJOR_VERSION, authorId.getBytes());
			will(returnValue(group));
		}});
	}
}
