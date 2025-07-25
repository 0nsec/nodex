package org.nodex.avatar;
import org.nodex.api.FormatException;
import org.nodex.api.Pair;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager.ContactHook;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Metadata;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Group.Visibility;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.validation.IncomingMessageHook;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.versioning.ClientVersioningManager.ClientVersioningHook;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.avatar.AvatarMessageEncoder;
import org.nodex.api.avatar.event.AvatarUpdatedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.sync.validation.IncomingMessageHookConstants.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.avatar.AvatarConstants.GROUP_KEY_CONTACT_ID;
import static org.nodex.avatar.AvatarConstants.MSG_KEY_VERSION;
import static org.nodex.api.avatar.AvatarManager.CLIENT_ID;
import static org.nodex.api.avatar.AvatarManager.MAJOR_VERSION;
@Immutable
@NotNullByDefault
class AvatarManagerImpl implements AvatarManager, OpenDatabaseHook, ContactHook,
		ClientVersioningHook, IncomingMessageHook {

	@Override
	public void onContactAdded(Contact contact) {
		// TODO: Implement logic for when a contact is added
	}

	@Override
	public void onContactRemoved(Contact contact) {
		// TODO: Implement logic for when a contact is removed
	}

	@Override
	public void onContactUpdated(Contact contact) {
		// TODO: Implement logic for when a contact is updated
	}
	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final ClientHelper clientHelper;
	private final ClientVersioningManager clientVersioningManager;
	private final MetadataParser metadataParser;
	private final GroupFactory groupFactory;
	private final AvatarMessageEncoder avatarMessageEncoder;
	@Inject
	AvatarManagerImpl(
			DatabaseComponent db,
			IdentityManager identityManager,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser,
			GroupFactory groupFactory,
			AvatarMessageEncoder avatarMessageEncoder) {
		this.db = db;
		this.identityManager = identityManager;
		this.clientHelper = clientHelper;
		this.clientVersioningManager = clientVersioningManager;
		this.metadataParser = metadataParser;
		this.groupFactory = groupFactory;
		this.avatarMessageEncoder = avatarMessageEncoder;
	}
	@Override
	public void onDatabaseOpened() throws DbException {
		// Implementation for parameter-less version
	}
	
	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		LocalAuthor a = identityManager.getLocalAuthor(txn);
		Group ourGroup = getGroup(a.getId());
		if (db.containsGroup(txn, ourGroup.getId())) return;
		db.addGroup(txn, ourGroup);
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}
	
	public void addingContact(Transaction txn, Contact c) throws DbException {
		Group theirGroup = getGroup(c.getAuthor().getId());
		db.addGroup(txn, theirGroup);
		BdfDictionary d = new BdfDictionary();
		d.put(GROUP_KEY_CONTACT_ID, c.getId().getInt());
		try {
			clientHelper.mergeGroupMetadata(txn, theirGroup.getId(), d);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
		Group ourGroup = getOurGroup(txn);
		org.nodex.api.sync.Visibility client = clientVersioningManager.getClientVisibility(txn,
				c.getId(), CLIENT_ID.getString(), MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), ourGroup.getId(), client);
		db.setGroupVisibility(txn, c.getId(), theirGroup.getId(), client);
	}
	public void removingContact(Transaction txn, Contact c) throws DbException {
		db.removeGroup(txn, getGroup(c.getAuthor().getId()));
	}
	
	@Override
	public void onClientVersionUpdated(String clientId, int majorVersion, int minorVersion) {
		// Implementation for ClientVersioningHook
	}
	@Override
	public DeliveryAction incomingMessage(Transaction txn, Message m,
			Metadata meta) throws DbException, InvalidMessageException {
		Group ourGroup = getOurGroup(txn);
		if (m.getGroupId().equals(ourGroup.getId())) {
			throw new InvalidMessageException(
					"Received incoming message in my avatar group");
		}
		try {
			BdfDictionary d = metadataParser.toDict(meta);
			LatestUpdate latest = findLatest(txn, m.getGroupId());
			if (latest != null) {
				if (d.getLong(MSG_KEY_VERSION) > latest.version) {
					db.deleteMessage(txn, latest.messageId);
					db.deleteMessageMetadata(txn, latest.messageId);
				} else {
					db.deleteMessage(txn, m.getId());
					db.deleteMessageMetadata(txn, m.getId());
					return IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
				}
			}
			ContactId contactId = getContactId(txn, m.getGroupId());
			String contentType = d.getString(MSG_KEY_CONTENT_TYPE);
			AttachmentHeader a = new AttachmentHeader(m.getGroupId(), m.getId(),
					contentType);
			txn.attach(new AvatarUpdatedEvent(contactId, a));
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
		return IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
	}
	@Override
	public AttachmentHeader addAvatar(String contentType, InputStream in)
			throws DbException, IOException {
		GroupId groupId;
		LatestUpdate latest;
		Transaction txn = db.startTransaction(true);
		try {
			groupId = getOurGroup(txn).getId();
			try {
				latest = findLatest(txn, groupId);
			} catch (FormatException e) {
				throw new DbException(e);
			}
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
		long version = latest == null ? 0 : latest.version + 1;
		Pair<Message, BdfDictionary> encodedMessage = avatarMessageEncoder
				.encodeUpdateMessage(groupId, version, contentType, in);
		Message m = encodedMessage.getFirst();
		BdfDictionary meta = encodedMessage.getSecond();
		return db.transactionWithResult(false, txn2 -> {
			try {
				LatestUpdate newLatest = findLatest(txn2, groupId);
				if (newLatest != null && newLatest.version > version) {
					return new AttachmentHeader(groupId, newLatest.messageId,
							newLatest.contentType);
				} else if (newLatest != null) {
					db.deleteMessage(txn2, newLatest.messageId);
					db.deleteMessageMetadata(txn2, newLatest.messageId);
				}
				clientHelper.addLocalMessage(txn2, m, meta, true, false);
				return new AttachmentHeader(groupId, m.getId(), contentType);
			} catch (FormatException e) {
				throw new DbException(e);
			}
		});
	}
	@Nullable
	@Override
	public AttachmentHeader getAvatarHeader(Transaction txn, Contact c)
			throws DbException {
		try {
			Group g = getGroup(c.getAuthor().getId());
			return getAvatarHeader(txn, g.getId());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Nullable
	@Override
	public AttachmentHeader getMyAvatarHeader(Transaction txn)
			throws DbException {
		try {
			Group g = getOurGroup(txn);
			return getAvatarHeader(txn, g.getId());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Nullable
	private AttachmentHeader getAvatarHeader(Transaction txn, GroupId groupId)
			throws DbException, FormatException {
		LatestUpdate latest = findLatest(txn, groupId);
		if (latest == null) return null;
		return new AttachmentHeader(groupId, latest.messageId,
				latest.contentType);
	}
	@Nullable
	private LatestUpdate findLatest(Transaction txn, GroupId g)
			throws DbException, FormatException {
		BdfDictionary query = new BdfDictionary();
		Map<MessageId, BdfDictionary> metadata =
				clientHelper.getMessageMetadataAsDictionary(txn, g, query);
		for (Map.Entry<MessageId, BdfDictionary> e : metadata.entrySet()) {
			BdfDictionary meta = e.getValue();
			long version = meta.getLong(MSG_KEY_VERSION);
			String contentType = meta.getString(MSG_KEY_CONTENT_TYPE);
			return new LatestUpdate(e.getKey(), version, contentType);
		}
		return null;
	}
	private ContactId getContactId(Transaction txn, GroupId g)
			throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g);
			return new ContactId(meta.getInt(GROUP_KEY_CONTACT_ID));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private Group getOurGroup(Transaction txn) throws DbException {
		LocalAuthor a = identityManager.getLocalAuthor(txn);
		return getGroup(a.getId());
	}
	private Group getGroup(AuthorId authorId) {
		return groupFactory
				.createGroup(CLIENT_ID, MAJOR_VERSION, authorId.getBytes());
	}
	private static class LatestUpdate {
		private final MessageId messageId;
		private final long version;
		private final String contentType;
		private LatestUpdate(MessageId messageId, long version,
				String contentType) {
			this.messageId = messageId;
			this.version = version;
			this.contentType = contentType;
		}
	}
}
