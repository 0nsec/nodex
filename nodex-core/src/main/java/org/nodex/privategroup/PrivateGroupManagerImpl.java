package org.nodex.privategroup;
import org.nodex.api.FormatException;
import org.nodex.api.client.BdfIncomingMessageHook;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.event.Event;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorInfo.Status;
import org.nodex.api.identity.AuthorManager;
import org.nodex.api.privategroup.GroupMember;
import org.nodex.api.privategroup.GroupMessage;
import org.nodex.api.privategroup.GroupMessageHeader;
import org.nodex.api.privategroup.JoinMessageHeader;
import org.nodex.api.privategroup.MessageType;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.Visibility;
import org.nodex.api.privategroup.event.ContactRelationshipRevealedEvent;
import org.nodex.api.privategroup.event.GroupDissolvedEvent;
import org.nodex.api.privategroup.event.GroupMessageAddedEvent;
import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import static org.nodex.api.sync.validation.IncomingMessageHookConstants.DeliveryAction.ACCEPT_SHARE;
import static org.nodex.api.identity.AuthorInfo.Status.UNVERIFIED;
import static org.nodex.api.identity.AuthorInfo.Status.VERIFIED;
import static org.nodex.api.privategroup.MessageType.JOIN;
import static org.nodex.api.privategroup.MessageType.POST;
import static org.nodex.api.privategroup.Visibility.INVISIBLE;
import static org.nodex.api.privategroup.Visibility.REVEALED_BY_CONTACT;
import static org.nodex.api.privategroup.Visibility.REVEALED_BY_US;
import static org.nodex.api.privategroup.Visibility.VISIBLE;
import static org.nodex.privategroup.GroupConstants.GROUP_KEY_CREATOR_ID;
import static org.nodex.privategroup.GroupConstants.GROUP_KEY_DISSOLVED;
import static org.nodex.privategroup.GroupConstants.GROUP_KEY_MEMBERS;
import static org.nodex.privategroup.GroupConstants.GROUP_KEY_OUR_GROUP;
import static org.nodex.privategroup.GroupConstants.GROUP_KEY_VISIBILITY;
import static org.nodex.privategroup.GroupConstants.KEY_INITIAL_JOIN_MSG;
import static org.nodex.privategroup.GroupConstants.KEY_MEMBER;
import static org.nodex.privategroup.GroupConstants.KEY_PARENT_MSG_ID;
import static org.nodex.privategroup.GroupConstants.KEY_PREVIOUS_MSG_ID;
import static org.nodex.privategroup.GroupConstants.KEY_READ;
import static org.nodex.privategroup.GroupConstants.KEY_TIMESTAMP;
import static org.nodex.privategroup.GroupConstants.KEY_TYPE;
import static org.nodex.api.privategroup.PrivateGroupManager.CLIENT_ID;
import static org.nodex.api.privategroup.PrivateGroupManager.MAJOR_VERSION;

@ThreadSafe
@NotNullByDefault
class PrivateGroupManagerImpl extends BdfIncomingMessageHook
		implements PrivateGroupManager {
	private final PrivateGroupFactory privateGroupFactory;
	private final ContactManager contactManager;
	private final IdentityManager identityManager;
	private final AuthorManager authorManager;
	private final MessageTracker messageTracker;
	private final List<PrivateGroupHook> hooks;
	@Inject
	PrivateGroupManagerImpl(ClientHelper clientHelper,
			MetadataParser metadataParser, DatabaseComponent db,
			PrivateGroupFactory privateGroupFactory,
			ContactManager contactManager, IdentityManager identityManager,
			AuthorManager authorManager, MessageTracker messageTracker) {
		super(CLIENT_ID, MAJOR_VERSION);
		this.privateGroupFactory = privateGroupFactory;
		this.contactManager = contactManager;
		this.identityManager = identityManager;
		this.authorManager = authorManager;
		this.messageTracker = messageTracker;
		hooks = new CopyOnWriteArrayList<>();
	}
	@Override
	public void addPrivateGroup(PrivateGroup group, GroupMessage joinMsg,
			boolean creator) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			addPrivateGroup(txn, group, joinMsg, creator);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
	}
	@Override
	public void addPrivateGroup(Transaction txn, PrivateGroup group,
			GroupMessage joinMsg, boolean creator) throws DbException {
		try {
			db.addGroup(txn, group.getGroup());
			AuthorId creatorId = group.getCreator().getId();
			BdfDictionary meta = BdfDictionary.of(
					BdfEntry.of(GROUP_KEY_MEMBERS, new BdfList()),
					BdfEntry.of(GROUP_KEY_CREATOR_ID, creatorId),
					BdfEntry.of(GROUP_KEY_OUR_GROUP, creator),
					BdfEntry.of(GROUP_KEY_DISSOLVED, false)
			);
			clientHelper.mergeGroupMetadata(txn, group.getId(), meta);
			joinPrivateGroup(txn, joinMsg, creator);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private void joinPrivateGroup(Transaction txn, GroupMessage m,
			boolean creator) throws DbException, FormatException {
		BdfDictionary meta = new BdfDictionary();
		meta.put(KEY_TYPE, JOIN.getInt());
		meta.put(KEY_INITIAL_JOIN_MSG, creator);
		addMessageMetadata(meta, m);
		clientHelper.addLocalMessage(txn, m.getMessage(), meta, true, false);
		messageTracker.trackOutgoingMessage(txn, m.getMessage());
		addMember(txn, m.getMessage().getGroupId(), m.getMember(), VISIBLE);
		setPreviousMsgId(txn, m.getMessage().getGroupId(),
				m.getMessage().getId());
		attachJoinMessageAddedEvent(txn, m.getMessage(), meta, true);
	}
	@Override
	public void removePrivateGroup(Transaction txn, GroupId g)
			throws DbException {
		for (PrivateGroupHook hook : hooks) {
			hook.removingGroup(txn, g);
		}
		Group group = db.getGroup(txn, g);
		db.removeGroup(txn, group);
	}
	@Override
	public void removePrivateGroup(GroupId g) throws DbException {
		db.transaction(false, txn -> removePrivateGroup(txn, g));
	}
	@Override
	public MessageId getPreviousMsgId(GroupId g) throws DbException {
		return db.transactionWithResult(true,
				txn -> getPreviousMsgId(txn, g));
	}
	public MessageId getPreviousMsgId(Transaction txn, GroupId g)
			throws DbException {
		try {
			BdfDictionary d = clientHelper.getGroupMetadataAsDictionary(txn, g);
			byte[] previousMsgIdBytes = d.getRaw(KEY_PREVIOUS_MSG_ID);
			return new MessageId(previousMsgIdBytes);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private void setPreviousMsgId(Transaction txn, GroupId g,
			MessageId previousMsgId) throws DbException, FormatException {
		BdfDictionary d = BdfDictionary
				.of(BdfEntry.of(KEY_PREVIOUS_MSG_ID, previousMsgId));
		clientHelper.mergeGroupMetadata(txn, g, d);
	}
	@Override
	public void markGroupDissolved(Transaction txn, GroupId g)
			throws DbException {
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(GROUP_KEY_DISSOLVED, true)
		);
		try {
			clientHelper.mergeGroupMetadata(txn, g, meta);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		Event e = new GroupDissolvedEvent(g);
		txn.attach(e);
	}
	@Override
	public GroupMessageHeader addLocalMessage(GroupMessage m)
			throws DbException {
		return db.transactionWithResult(false, txn -> addLocalMessage(txn, m));
	}
	@Override
	public GroupMessageHeader addLocalMessage(Transaction txn, GroupMessage m)
			throws DbException {
		try {
			BdfDictionary meta = new BdfDictionary();
			meta.put(KEY_TYPE, POST.getInt());
			if (m.getParent() != null)
				meta.put(KEY_PARENT_MSG_ID, m.getParent());
			addMessageMetadata(meta, m);
			GroupId g = m.getMessage().getGroupId();
			clientHelper
					.addLocalMessage(txn, m.getMessage(), meta, true, false);
			setPreviousMsgId(txn, g, m.getMessage().getId());
			messageTracker.trackOutgoingMessage(txn, m.getMessage());
			attachGroupMessageAddedEvent(txn, m.getMessage(), meta, true);
			AuthorInfo authorInfo = authorManager.getMyAuthorInfo(txn);
			return new GroupMessageHeader(m.getMessage().getGroupId(),
					m.getMessage().getId(), m.getParent(),
					m.getMessage().getTimestamp(), m.getMember(), authorInfo,
					true);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private void addMessageMetadata(BdfDictionary meta, GroupMessage m) {
		meta.put(KEY_MEMBER, clientHelper.toList(m.getMember()));
		meta.put(KEY_TIMESTAMP, m.getMessage().getTimestamp());
		meta.put(KEY_READ, true);
	}
	@Override
	public PrivateGroup getPrivateGroup(GroupId g) throws DbException {
		PrivateGroup privateGroup;
		Transaction txn = db.startTransaction(true);
		try {
			privateGroup = getPrivateGroup(txn, g);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
		return privateGroup;
	}
	@Override
	public PrivateGroup getPrivateGroup(Transaction txn, GroupId g)
			throws DbException {
		try {
			Group group = db.getGroup(txn, g);
			return privateGroupFactory.parsePrivateGroup(group);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public Collection<PrivateGroup> getPrivateGroups(Transaction txn)
			throws DbException {
		Collection<Group> groups = db.getGroups(txn, CLIENT_ID.toString(), MAJOR_VERSION);
		Collection<PrivateGroup> privateGroups = new ArrayList<>(groups.size());
		try {
			for (Group g : groups) {
				privateGroups.add(privateGroupFactory.parsePrivateGroup(g));
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return privateGroups;
	}
	@Override
	public boolean isOurPrivateGroup(Transaction txn, PrivateGroup g)
			throws DbException {
		LocalAuthor localAuthor = identityManager.getLocalAuthor(txn);
		return localAuthor.getId().equals(g.getCreator().getId());
	}
	@Override
	public Collection<PrivateGroup> getPrivateGroups() throws DbException {
		return db.transactionWithResult(true, this::getPrivateGroups);
	}
	@Override
	public boolean isDissolved(Transaction txn, GroupId g) throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g);
			return meta.getBoolean(GROUP_KEY_DISSOLVED);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public boolean isDissolved(GroupId g) throws DbException {
		return db.transactionWithResult(true, txn -> isDissolved(txn, g));
	}
	@Override
	public String getMessageText(MessageId m) throws DbException {
		try {
			return getMessageText(clientHelper.getMessageAsList(m));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public String getMessageText(Transaction txn, MessageId m)
			throws DbException {
		try {
			return getMessageText(clientHelper.getMessageAsList(txn, m));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private String getMessageText(BdfList body) throws FormatException {
		return body.getString(4);
	}
	@Override
	public Collection<GroupMessageHeader> getHeaders(GroupId g)
			throws DbException {
		return db.transactionWithResult(true, txn -> getHeaders(txn, g));
	}
	@Override
	public List<GroupMessageHeader> getHeaders(Transaction txn, GroupId g)
			throws DbException {
		List<GroupMessageHeader> headers = new ArrayList<>();
		try {
			Map<MessageId, BdfDictionary> metadata =
					clientHelper.getMessageMetadataAsDictionary(txn, g);
			Set<AuthorId> authors = new HashSet<>();
			for (BdfDictionary meta : metadata.values()) {
				authors.add(getAuthor(meta).getId());
			}
			Map<AuthorId, AuthorInfo> authorInfos = new HashMap<>();
			for (AuthorId id : authors) {
				authorInfos.put(id, authorManager.getAuthorInfo(txn, id));
			}
			for (Entry<MessageId, BdfDictionary> entry : metadata.entrySet()) {
				BdfDictionary meta = entry.getValue();
				if (meta.getInt(KEY_TYPE) == JOIN.getInt()) {
					headers.add(getJoinMessageHeader(txn, g, entry.getKey(),
							meta, authorInfos));
				} else {
					headers.add(getGroupMessageHeader(txn, g, entry.getKey(),
							meta, authorInfos));
				}
			}
			return headers;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private GroupMessageHeader getGroupMessageHeader(Transaction txn, GroupId g,
			MessageId id, BdfDictionary meta,
			Map<AuthorId, AuthorInfo> authorInfos)
			throws DbException, FormatException {
		MessageId parentId = null;
		if (meta.containsKey(KEY_PARENT_MSG_ID)) {
			parentId = new MessageId(meta.getRaw(KEY_PARENT_MSG_ID));
		}
		long timestamp = meta.getLong(KEY_TIMESTAMP);
		Author member = getAuthor(meta);
		AuthorInfo authorInfo;
		if (authorInfos.containsKey(member.getId())) {
			authorInfo = authorInfos.get(member.getId());
		} else {
			authorInfo = authorManager.getAuthorInfo(txn, member.getId());
		}
		boolean read = meta.getBoolean(KEY_READ);
		return new GroupMessageHeader(g, id, parentId, timestamp, member,
				authorInfo, read);
	}
	private JoinMessageHeader getJoinMessageHeader(Transaction txn, GroupId g,
			MessageId id, BdfDictionary meta,
			Map<AuthorId, AuthorInfo> authorInfos)
			throws DbException, FormatException {
		GroupMessageHeader header =
				getGroupMessageHeader(txn, g, id, meta, authorInfos);
		boolean creator = meta.getBoolean(KEY_INITIAL_JOIN_MSG);
		return new JoinMessageHeader(header, creator);
	}
	@Override
	public Collection<GroupMember> getMembers(GroupId g) throws DbException {
		return db.transactionWithResult(true, txn -> getMembers(txn, g));
	}
	@Override
	public Collection<GroupMember> getMembers(Transaction txn, GroupId g)
			throws DbException {
		Collection<GroupMember> members = new ArrayList<>();
		Map<Author, Visibility> authors = getMemberAuthors(txn, g);
		LocalAuthor la = identityManager.getLocalAuthor(txn);
		PrivateGroup privateGroup = getPrivateGroup(txn, g);
		for (Entry<Author, Visibility> m : authors.entrySet()) {
			Author a = m.getKey();
			AuthorInfo authorInfo = authorManager.getAuthorInfo(txn, a.getId());
			Status status = authorInfo.getStatus();
			Visibility v = m.getValue();
			ContactId c = null;
			if (v != INVISIBLE &&
					(status == VERIFIED || status == UNVERIFIED)) {
				c = contactManager.getContact(txn, a.getId(), la.getId())
						.getId();
			}
			boolean isCreator = privateGroup.getCreator().equals(a);
			members.add(new GroupMember(a, authorInfo, isCreator, c, v));
		}
		return members;
	}
	private Map<Author, Visibility> getMemberAuthors(Transaction txn, GroupId g)
			throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g);
			BdfList list = meta.getList(GROUP_KEY_MEMBERS);
			Map<Author, Visibility> members = new HashMap<>(list.size());
			for (int i = 0; i < list.size(); i++) {
				BdfDictionary d = list.getDictionary(i);
				Author member = getAuthor(d);
				Visibility v = getVisibility(d);
				members.put(member, v);
			}
			return members;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public boolean isMember(Transaction txn, GroupId g, Author a)
			throws DbException {
		for (Author member : getMemberAuthors(txn, g).keySet()) {
			if (member.equals(a)) return true;
		}
		return false;
	}
	@Override
	public GroupCount getGroupCount(Transaction txn, GroupId g)
			throws DbException {
		return messageTracker.getGroupCount(txn, g);
	}
	@Override
	public GroupCount getGroupCount(GroupId g) throws DbException {
		return messageTracker.getGroupCount(g);
	}
	@Override
	public void setReadFlag(Transaction txn, GroupId g, MessageId m,
			boolean read) throws DbException {
		messageTracker.setReadFlag(txn, g, m, read);
	}
	@Override
	public void setReadFlag(GroupId g, MessageId m, boolean read)
			throws DbException {
		db.transaction(false, txn -> setReadFlag(txn, g, m, read));
	}
	@Override
	public void relationshipRevealed(Transaction txn, GroupId g, AuthorId a,
			boolean byContact) throws FormatException, DbException {
		BdfDictionary meta = clientHelper.getGroupMetadataAsDictionary(txn, g);
		BdfList members = meta.getList(GROUP_KEY_MEMBERS);
		Visibility v = INVISIBLE;
		boolean foundMember = false, changed = false;
		for (int i = 0; i < members.size(); i++) {
			BdfDictionary d = members.getDictionary(i);
			if (a.equals(getAuthor(d).getId())) {
				foundMember = true;
				if (getVisibility(d) == INVISIBLE) {
					changed = true;
					v = byContact ? REVEALED_BY_CONTACT : REVEALED_BY_US;
					d.put(GROUP_KEY_VISIBILITY, v.getInt());
				}
				break;
			}
		}
		if (!foundMember) throw new ProtocolStateException();
		if (changed) {
			clientHelper.mergeGroupMetadata(txn, g, meta);
			LocalAuthor la = identityManager.getLocalAuthor(txn);
			ContactId c = contactManager.getContact(txn, a, la.getId()).getId();
			Event e = new ContactRelationshipRevealedEvent(g, a, c, v);
			txn.attach(e);
		}
	}
	@Override
	public void registerPrivateGroupHook(PrivateGroupHook hook) {
		hooks.add(hook);
	}
	@Override
	protected DeliveryAction incomingMessage(Transaction txn, Message m,
			BdfList body, BdfDictionary meta)
			throws DbException, FormatException {
		MessageType type = MessageType.valueOf(meta.getInt(KEY_TYPE));
		switch (type) {
			case JOIN:
				handleJoinMessage(txn, m, meta);
				return ACCEPT_SHARE;
			case POST:
				handleGroupMessage(txn, m, meta);
				return ACCEPT_SHARE;
			default:
				throw new RuntimeException("Unknown MessageType");
		}
	}
	private void handleJoinMessage(Transaction txn, Message m,
			BdfDictionary meta) throws FormatException, DbException {
		Author member = getAuthor(meta);
		BdfDictionary groupMeta = clientHelper
				.getGroupMetadataAsDictionary(txn, m.getGroupId());
		boolean ourGroup = groupMeta.getBoolean(GROUP_KEY_OUR_GROUP);
		Visibility v = VISIBLE;
		if (!ourGroup) {
			AuthorId creatorId = new AuthorId(
					groupMeta.getRaw(GROUP_KEY_CREATOR_ID));
			if (!creatorId.equals(member.getId()))
				v = INVISIBLE;
		}
		addMember(txn, m.getGroupId(), member, v);
		messageTracker.trackIncomingMessage(txn, m);
		attachJoinMessageAddedEvent(txn, m, meta, false);
	}
	private void handleGroupMessage(Transaction txn, Message m,
			BdfDictionary meta) throws FormatException, DbException {
		long timestamp = meta.getLong(KEY_TIMESTAMP);
		byte[] parentIdBytes = meta.getOptionalRaw(KEY_PARENT_MSG_ID);
		if (parentIdBytes != null) {
			MessageId parentId = new MessageId(parentIdBytes);
			BdfDictionary parentMeta = clientHelper
					.getMessageMetadataAsDictionary(txn, parentId);
			if (timestamp <= parentMeta.getLong(KEY_TIMESTAMP))
				throw new FormatException();
			MessageType parentType =
					MessageType.valueOf(parentMeta.getInt(KEY_TYPE));
			if (parentType != POST)
				throw new FormatException();
		}
		byte[] previousMsgIdBytes = meta.getRaw(KEY_PREVIOUS_MSG_ID);
		MessageId previousMsgId = new MessageId(previousMsgIdBytes);
		BdfDictionary previousMeta = clientHelper
				.getMessageMetadataAsDictionary(txn, previousMsgId);
		if (timestamp <= previousMeta.getLong(KEY_TIMESTAMP))
			throw new FormatException();
		if (!getAuthor(meta).equals(getAuthor(previousMeta)))
			throw new FormatException();
		MessageType previousType =
				MessageType.valueOf(previousMeta.getInt(KEY_TYPE));
		if (previousType != JOIN && previousType != POST)
			throw new FormatException();
		messageTracker.trackIncomingMessage(txn, m);
		attachGroupMessageAddedEvent(txn, m, meta, false);
	}
	private void attachGroupMessageAddedEvent(Transaction txn, Message m,
			BdfDictionary meta, boolean local)
			throws DbException, FormatException {
		GroupMessageHeader header = getGroupMessageHeader(txn, m.getGroupId(),
				m.getId(), meta, Collections.emptyMap());
		String text = getMessageText(clientHelper.toList(m));
		txn.attach(new GroupMessageAddedEvent(m.getGroupId(), header, text,
				local));
	}
	private void attachJoinMessageAddedEvent(Transaction txn, Message m,
			BdfDictionary meta, boolean local)
			throws DbException, FormatException {
		JoinMessageHeader header = getJoinMessageHeader(txn, m.getGroupId(),
				m.getId(), meta, Collections.emptyMap());
		txn.attach(new GroupMessageAddedEvent(m.getGroupId(), header, "",
				local));
	}
	private void addMember(Transaction txn, GroupId g, Author a, Visibility v)
			throws DbException, FormatException {
		BdfDictionary meta = clientHelper.getGroupMetadataAsDictionary(txn, g);
		BdfList members = meta.getList(GROUP_KEY_MEMBERS);
		members.add(BdfDictionary.of(
				BdfEntry.of(KEY_MEMBER, clientHelper.toList(a)),
				BdfEntry.of(GROUP_KEY_VISIBILITY, v.getInt())
		));
		clientHelper.mergeGroupMetadata(txn, g, meta);
		for (PrivateGroupHook hook : hooks) {
			hook.addingMember(txn, g, a);
		}
	}
	private Author getAuthor(BdfDictionary meta) throws FormatException {
		return clientHelper.parseAndValidateAuthor(meta.getList(KEY_MEMBER));
	}
	private Visibility getVisibility(BdfDictionary meta)
			throws FormatException {
		return Visibility.valueOf(meta.getInt(GROUP_KEY_VISIBILITY));
	}
}
