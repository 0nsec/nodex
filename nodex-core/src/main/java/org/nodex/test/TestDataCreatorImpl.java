package org.nodex.test;
import org.nodex.api.FeatureFlags;
import org.nodex.api.FormatException;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.SecretKey;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.lifecycle.IoExecutor;
import org.nodex.api.plugin.BluetoothConstants;
import org.nodex.api.plugin.LanTcpConstants;
import org.nodex.api.plugin.TorConstants;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.properties.TransportPropertyManager;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.avatar.AvatarMessageEncoder;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPost;
import org.nodex.api.blog.BlogPostFactory;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumPost;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.api.privategroup.GroupMessage;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationFactory;
import org.nodex.api.test.TestAvatarCreator;
import org.nodex.api.test.TestDataCreator;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.api.plugin.BluetoothConstants.UUID_BYTES;
import static org.nodex.api.sync.Group.Visibility.SHARED;
import static org.nodex.api.util.LogUtils.logException;
import static org.nodex.api.util.StringUtils.getRandomString;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.test.TestData.AUTHOR_NAMES;
import static org.nodex.test.TestData.GROUP_NAMES;
@NotNullByDefault
public class TestDataCreatorImpl implements TestDataCreator {
	private final Logger LOG =
			getLogger(TestDataCreatorImpl.class.getName());
	private final AuthorFactory authorFactory;
	private final Clock clock;
	private final GroupFactory groupFactory;
	private final PrivateMessageFactory privateMessageFactory;
	private final BlogPostFactory blogPostFactory;
	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final CryptoComponent crypto;
	private final ContactManager contactManager;
	private final TransportPropertyManager transportPropertyManager;
	private final MessagingManager messagingManager;
	private final BlogManager blogManager;
	private final ForumManager forumManager;
	private final PrivateGroupManager privateGroupManager;
	private final PrivateGroupFactory privateGroupFactory;
	private final GroupMessageFactory groupMessageFactory;
	private final GroupInvitationFactory groupInvitationFactory;
	private final TestAvatarCreator testAvatarCreator;
	private final AvatarMessageEncoder avatarMessageEncoder;
	private final FeatureFlags featureFlags;
	private final Executor ioExecutor;
	private final Random random = new Random();
	private final Map<Contact, LocalAuthor> localAuthors = new HashMap<>();
	@Inject
	TestDataCreatorImpl(AuthorFactory authorFactory, Clock clock,
			GroupFactory groupFactory,
			PrivateMessageFactory privateMessageFactory,
			BlogPostFactory blogPostFactory, DatabaseComponent db,
			IdentityManager identityManager,
			CryptoComponent crypto,
			ContactManager contactManager,
			TransportPropertyManager transportPropertyManager,
			MessagingManager messagingManager, BlogManager blogManager,
			ForumManager forumManager,
			PrivateGroupManager privateGroupManager,
			PrivateGroupFactory privateGroupFactory,
			GroupMessageFactory groupMessageFactory,
			GroupInvitationFactory groupInvitationFactory,
			TestAvatarCreator testAvatarCreator,
			AvatarMessageEncoder avatarMessageEncoder,
			FeatureFlags featureFlags,
			Executor ioExecutor) {
		this.authorFactory = authorFactory;
		this.clock = clock;
		this.groupFactory = groupFactory;
		this.privateMessageFactory = privateMessageFactory;
		this.blogPostFactory = blogPostFactory;
		this.db = db;
		this.identityManager = identityManager;
		this.crypto = crypto;
		this.contactManager = contactManager;
		this.transportPropertyManager = transportPropertyManager;
		this.messagingManager = messagingManager;
		this.blogManager = blogManager;
		this.forumManager = forumManager;
		this.privateGroupManager = privateGroupManager;
		this.privateGroupFactory = privateGroupFactory;
		this.groupMessageFactory = groupMessageFactory;
		this.groupInvitationFactory = groupInvitationFactory;
		this.testAvatarCreator = testAvatarCreator;
		this.avatarMessageEncoder = avatarMessageEncoder;
		this.featureFlags = featureFlags;
		this.ioExecutor = ioExecutor;
	}
	@Override
	public void createTestData(int numContacts, int numPrivateMsgs,
			int avatarPercent, int numBlogPosts, int numForums,
			int numForumPosts, int numPrivateGroups,
			int numPrivateGroupMessages) {
		if (numContacts == 0) throw new IllegalArgumentException();
		if (avatarPercent < 0 || avatarPercent > 100)
			throw new IllegalArgumentException();
		ioExecutor.execute(() -> {
			try {
				createTestDataOnIoExecutor(numContacts, numPrivateMsgs,
						avatarPercent, numBlogPosts, numForums, numForumPosts,
						numPrivateGroups, numPrivateGroupMessages);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	
	private void createTestDataOnIoExecutor(int numContacts, int numPrivateMsgs,
			int avatarPercent, int numBlogPosts, int numForums,
			int numForumPosts, int numPrivateGroups,
			int numPrivateGroupMessages) throws DbException {
		List<Contact> contacts = createContacts(numContacts, avatarPercent);
		createPrivateMessages(contacts, numPrivateMsgs);
		createBlogPosts(contacts, numBlogPosts);
		List<Forum> forums = createForums(contacts, numForums);
		for (Forum forum : forums) {
			createRandomForumPosts(forum, contacts, numForumPosts);
		}
		List<PrivateGroup> groups =
				createPrivateGroups(contacts, numPrivateGroups);
		for (PrivateGroup group : groups) {
			createRandomPrivateGroupMessages(group, contacts,
					numPrivateGroupMessages);
		}
	}
	private List<Contact> createContacts(int numContacts, int avatarPercent)
			throws DbException {
		List<Contact> contacts = new ArrayList<>(numContacts);
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		for (int i = 0; i < numContacts; i++) {
			LocalAuthor remote = getRandomAuthor();
			Contact contact = addContact(localAuthor.getId(), remote,
					random.nextBoolean(), avatarPercent);
			contacts.add(contact);
		}
		return contacts;
	}
	private Contact addContact(AuthorId localAuthorId, LocalAuthor remote,
			boolean alias, int avatarPercent) throws DbException {
		SecretKey secretKey = getSecretKey();
		long timestamp = clock.currentTimeMillis();
		boolean verified = random.nextBoolean();
		Map<TransportId, TransportProperties> props =
				getRandomTransportProperties();
		Contact contact = db.transactionWithResult(false, txn -> {
			ContactId contactId = contactManager.addContact(txn, remote,
					localAuthorId, secretKey, timestamp, true, verified, true);
			if (alias) {
				contactManager.setContactAlias(txn, contactId,
						getRandomAuthorName());
			}
			transportPropertyManager.addRemoteProperties(txn, contactId, props);
			return db.getContact(txn, contactId);
		});
		if (random.nextInt(100) + 1 <= avatarPercent) addAvatar(contact);
		if (LOG.isLoggable(INFO)) {
			LOG.info("Added contact " + remote.getName() +
					" with transport properties: " + props);
		}
		localAuthors.put(contact, remote);
		return contact;
	}
	@Override
	public Contact createContact() {
		try {
			return addContact(getRandomAuthorName(), false, false);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Contact createContact(ContactId contactId) {
		// This implementation creates a contact with a random name
		// In a real implementation, this would use the provided contactId
		return createContact();
	}

	@Override
	public LocalAuthor createLocalAuthor() {
		return getRandomAuthor();
	}

	@Override
	public LocalAuthor createLocalAuthor(String name) {
		return authorFactory.createLocalAuthor(name);
	}

	@Override
	public Contact addContact(String name, boolean alias, boolean avatar)
			throws DbException {
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		LocalAuthor remote = authorFactory.createLocalAuthor(name);
		int avatarPercent = avatar ? 100 : 0;
		return addContact(localAuthor.getId(), remote, alias, avatarPercent);
	}
	private String getRandomAuthorName() {
		int i = random.nextInt(AUTHOR_NAMES.length);
		return AUTHOR_NAMES[i];
	}
	private LocalAuthor getRandomAuthor() {
		return authorFactory.createLocalAuthor(getRandomAuthorName());
	}
	private SecretKey getSecretKey() {
		byte[] b = new byte[SecretKey.LENGTH];
		random.nextBytes(b);
		return new SecretKey(b);
	}
	private Map<TransportId, TransportProperties> getRandomTransportProperties() {
		Map<TransportId, TransportProperties> props = new HashMap<>();
		
		// Bluetooth properties
		TransportProperties bt = new TransportProperties();
		String btAddress = getRandomBluetoothAddress();
		String uuid = getRandomUUID();
		bt.put(BluetoothConstants.PROP_ADDRESS, btAddress);
		bt.put(BluetoothConstants.PROP_UUID, uuid);
		props.put(BluetoothConstants.ID, bt);
		
		// LAN properties
		TransportProperties lan = new TransportProperties();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			if (sb.length() > 0) sb.append(',');
			sb.append(getRandomLanAddress());
		}
		lan.put(LanTcpConstants.PROP_IP_PORTS, sb.toString());
		String port = String.valueOf(getRandomPortNumber());
		lan.put(LanTcpConstants.PROP_PORT, port);
		props.put(LanTcpConstants.ID, lan);
		
		// Tor properties
		TransportProperties tor = new TransportProperties();
		String torAddress = getRandomTorAddress();
		tor.put(TorConstants.PROP_ONION_V3, torAddress);
		props.put(TorConstants.ID, tor);
		
		return props;
	}
	private String getRandomBluetoothAddress() {
		byte[] mac = new byte[6];
		random.nextBytes(mac);
		StringBuilder sb = new StringBuilder(18);
		for (byte b : mac) {
			if (sb.length() > 0) sb.append(":");
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
	private String getRandomUUID() {
		byte[] uuid = new byte[UUID_BYTES];
		random.nextBytes(uuid);
		return UUID.nameUUIDFromBytes(uuid).toString();
	}
	private String getRandomLanAddress() {
		StringBuilder sb = new StringBuilder();
		if (random.nextInt(5) == 0) {
			sb.append("10.");
			sb.append(random.nextInt(2)).append('.');
		} else {
			sb.append("192.168.");
		}
		sb.append(random.nextInt(2)).append('.');
		sb.append(random.nextInt(255));
		sb.append(':').append(getRandomPortNumber());
		return sb.toString();
	}
	private int getRandomPortNumber() {
		return 32768 + random.nextInt(32768);
	}
	private String getRandomTorAddress() {
		try {
			byte[] pubkeyBytes = crypto.generateSignatureKeyPair().getPublic().getEncoded();
			return crypto.encodeOnion(pubkeyBytes);
		} catch (Exception e) {
			// Fallback to a random onion address format
			return getRandomString(16) + ".onion";
		}
	}
	private void addAvatar(Contact c) throws DbException {
		AuthorId authorId = c.getAuthor().getId();
		GroupId groupId = groupFactory.createGroup(AvatarManager.CLIENT_ID,
				AvatarManager.MAJOR_VERSION, authorId.getBytes()).getId();
		InputStream is;
		try {
			is = testAvatarCreator.getAvatarInputStream();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return;
		}
		if (is == null) return;
		Message m;
		try {
			m = avatarMessageEncoder.encodeUpdateMessage(groupId, 0,
					"image/jpeg", is).getFirst();
		} catch (IOException e) {
			throw new DbException(e);
		}
		db.transaction(false, txn -> {
			db.setGroupVisibility(txn, c.getId(), groupId, SHARED);
			db.receiveMessage(txn, c.getId(), m);
		});
	}
	private void shareGroup(ContactId contactId, GroupId groupId)
			throws DbException {
		db.transaction(false, txn ->
				db.setGroupVisibility(txn, contactId, groupId, SHARED));
	}
	private void createPrivateMessages(List<Contact> contacts,
			int numPrivateMsgs) throws DbException {
		for (Contact contact : contacts) {
			GroupId groupId = messagingManager.getContactGroupId(contact);
			shareGroup(contact.getId(), group.getId());
			for (int i = 0; i < numPrivateMsgs; i++) {
				createRandomPrivateMessage(contact.getId(), group.getId(), i);
			}
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numPrivateMsgs +
					" private messages per contact.");
		}
	}
	private void createRandomPrivateMessage(ContactId contactId,
			GroupId groupId, int num) throws DbException {
		long timestamp = clock.currentTimeMillis() - (long) num * 60 * 1000;
		String text = getRandomText();
		boolean local = random.nextBoolean();
		boolean autoDelete = random.nextBoolean();
		createPrivateMessage(contactId, groupId, text, timestamp, local,
				autoDelete);
	}
	private void createPrivateMessage(ContactId contactId, GroupId groupId,
			String text, long timestamp, boolean local, boolean autoDelete)
			throws DbException {
		long timer = autoDelete ?
				MIN_AUTO_DELETE_TIMER_MS : NO_AUTO_DELETE_TIMER;
		try {
			PrivateMessage m = privateMessageFactory.createPrivateMessage(
					groupId, timestamp, text, emptyList(), timer);
			if (local) {
				messagingManager.addLocalMessage(m);
			} else {
				db.transaction(false, txn ->
						db.receiveMessage(txn, contactId, m.getMessage()));
			}
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	private void createBlogPosts(List<Contact> contacts, int numBlogPosts)
			throws DbException {
		if (!featureFlags.shouldEnableBlogsInCore()) return;
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		Blog ours = blogManager.getPersonalBlog(localAuthor);
		for (Contact contact : contacts) {
			Blog theirs = blogManager.getPersonalBlog(contact.getAuthor());
			shareGroup(contact.getId(), ours.getId());
			shareGroup(contact.getId(), theirs.getId());
		}
		for (int i = 0; i < numBlogPosts; i++) {
			Contact contact = contacts.get(random.nextInt(contacts.size()));
			LocalAuthor author = localAuthors.get(contact);
			addBlogPost(contact.getId(), author, i);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numBlogPosts + " blog posts.");
		}
	}
	private void addBlogPost(ContactId contactId, LocalAuthor author, int num)
			throws DbException {
		Blog blog = blogManager.getPersonalBlog(author);
		long timestamp = clock.currentTimeMillis() - (long) num * 60 * 1000;
		String text = getRandomText();
		try {
			BlogPost blogPost = blogPostFactory.createBlogPost(blog.getId(),
					timestamp, null, author, text);
			db.transaction(false, txn ->
					db.receiveMessage(txn, contactId, blogPost.getMessage()));
		} catch (FormatException | GeneralSecurityException e) {
			throw new AssertionError(e);
		}
	}
	private List<Forum> createForums(List<Contact> contacts, int numForums)
			throws DbException {
		if (!featureFlags.shouldEnableForumsInCore()) return emptyList();
		List<Forum> forums = new ArrayList<>(numForums);
		for (int i = 0; i < numForums; i++) {
			String name = GROUP_NAMES[random.nextInt(GROUP_NAMES.length)];
			Forum forum = forumManager.addForum(name);
			for (Contact contact : contacts) {
				shareGroup(contact.getId(), forum.getId());
			}
			forums.add(forum);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numForums + " forums.");
		}
		return forums;
	}
	private void createRandomForumPosts(Forum forum, List<Contact> contacts,
			int numForumPosts) throws DbException {
		List<ForumPost> posts = new ArrayList<>();
		for (int i = 0; i < numForumPosts; i++) {
			Contact contact = contacts.get(random.nextInt(contacts.size()));
			LocalAuthor author = localAuthors.get(contact);
			long timestamp = clock.currentTimeMillis() - (long) i * 60 * 1000;
			String text = getRandomText();
			MessageId parent = null;
			if (random.nextBoolean() && posts.size() > 0) {
				ForumPost parentPost = posts.get(random.nextInt(posts.size()));
				parent = parentPost.getMessage().getId();
			}
			ForumPost post = forumManager.createLocalPost(forum.getId(), text,
					timestamp, parent, author);
			posts.add(post);
			db.transaction(false, txn ->
					db.receiveMessage(txn, contact.getId(), post.getMessage()));
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numForumPosts + " forum posts.");
		}
	}
	private List<PrivateGroup> createPrivateGroups(List<Contact> contacts,
			int numPrivateGroups) throws DbException {
		if (!featureFlags.shouldEnablePrivateGroupsInCore()) return emptyList();
		List<PrivateGroup> groups = new ArrayList<>(numPrivateGroups);
		for (int i = 0; i < numPrivateGroups; i++) {
			String name = GROUP_NAMES[random.nextInt(GROUP_NAMES.length)];
			LocalAuthor creator = identityManager.getLocalAuthor();
			PrivateGroup group =
					privateGroupFactory.createPrivateGroup(name, creator);
			GroupMessage joinMsg = groupMessageFactory.createJoinMessage(
					group.getId(),
					clock.currentTimeMillis() - (long) (100 - i) * 60 * 1000,
					creator
			);
			privateGroupManager.addPrivateGroup(group, joinMsg, true);
			groups.add(group);
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + numPrivateGroups + " private groups.");
		}
		return groups;
	}
	private void createRandomPrivateGroupMessages(PrivateGroup group,
			List<Contact> contacts, int amount) throws DbException {
		List<GroupMessage> messages = new ArrayList<>();
		PrivateKey creatorPrivateKey =
				identityManager.getLocalAuthor().getPrivateKey();
		int numMembers = random.nextInt(contacts.size());
		if (numMembers == 0) numMembers++;
		Map<Contact, MessageId> membersLastMessage = new HashMap<>();
		List<Contact> members = new ArrayList<>(numMembers);
		for (int i = 0; i < numMembers; i++) {
			Contact contact = contacts.get(i);
			members.add(contact);
		}
		for (int i = 0; i < amount; i++) {
			Contact contact = members.get(random.nextInt(numMembers));
			LocalAuthor author = localAuthors.get(contact);
			long timestamp =
					clock.currentTimeMillis() -
							(long) (amount - i) * 60 * 1000;
			GroupMessage msg;
			if (!membersLastMessage.containsKey(contact)) {
				shareGroup(contact.getId(), group.getId());
				long inviteTimestamp = timestamp - 1;
				byte[] creatorSignature =
						groupInvitationFactory.signInvitation(contact,
								group.getId(), inviteTimestamp,
								creatorPrivateKey);
				msg = groupMessageFactory.createJoinMessage(group.getId(),
								timestamp, author, inviteTimestamp,
								creatorSignature);
			} else {
				String text = getRandomText();
				MessageId parent = null;
				if (random.nextBoolean() && messages.size() > 0) {
					GroupMessage parentMessage =
							messages.get(random.nextInt(messages.size()));
					parent = parentMessage.getMessage().getId();
				}
				MessageId lastMsg = membersLastMessage.get(contact);
				msg = groupMessageFactory.createGroupMessage(
						group.getId(), timestamp, parent, author, text,
						lastMsg);
				messages.add(msg);
			}
			membersLastMessage.put(contact, msg.getMessage().getId());
			db.transaction(false, txn ->
					db.receiveMessage(txn, contact.getId(), msg.getMessage()));
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("Created " + amount + " private group messages.");
		}
	}
	private String getRandomText() {
		int minLength = 3 + random.nextInt(500);
		int maxWordLength = 15;
		StringBuilder sb = new StringBuilder();
		while (sb.length() < minLength) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(getRandomString(random.nextInt(maxWordLength) + 1));
		}
		if (random.nextBoolean()) {
			sb.append(" \uD83D\uDC96 \uD83E\uDD84 \uD83C\uDF08");
		}
		return sb.toString();
	}
}
