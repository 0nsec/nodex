package org.nodex.conversation;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.event.Event;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.DeletionResult;
import org.nodex.api.conversation.event.ConversationMessageTrackedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import static java.lang.Math.max;
@ThreadSafe
@NotNullByDefault
class ConversationManagerImpl implements ConversationManager {
	private final DatabaseComponent db;
	private final MessageTracker messageTracker;
	private final Clock clock;
	private final ClientHelper clientHelper;
	private final Set<ConversationClient> clients;
	@Inject
	ConversationManagerImpl(DatabaseComponent db, MessageTracker messageTracker,
			Clock clock, ClientHelper clientHelper) {
		this.db = db;
		this.messageTracker = messageTracker;
		this.clock = clock;
		this.clientHelper = clientHelper;
		clients = new CopyOnWriteArraySet<>();
	}
	@Override
	public void registerConversationClient(ConversationClient client) {
		if (!clients.add(client))
			throw new IllegalStateException("Client is already registered");
	}
	@Override
	public Collection<ConversationMessageHeader> getMessageHeaders(ContactId c)
			throws DbException {
		return db.transactionWithResult(true,
				txn -> getMessageHeaders(txn, c));
	}
	@Override
	public Collection<ConversationMessageHeader> getMessageHeaders(Transaction txn, ContactId c)
			throws DbException {
		List<ConversationMessageHeader> messages = new ArrayList<>();
		for (ConversationClient client : clients) {
			messages.addAll(client.getMessageHeaders(txn, c));
		}
		return messages;
	}
	@Override
	public GroupCount getGroupCount(ContactId contactId) throws DbException {
		return db.transactionWithResult(true,
				txn -> getGroupCount(txn, contactId));
	}
	@Override
	public GroupCount getGroupCount(Transaction txn, ContactId contactId)
			throws DbException {
		int msgCount = 0, unreadCount = 0;
		long latestTime = 0;
		for (ConversationClient client : clients) {
			GroupCount count = client.getGroupCount(txn, contactId);
			msgCount += count.getMsgCount();
			unreadCount += count.getUnreadCount();
			if (count.getLatestMsgTime() > latestTime)
				latestTime = count.getLatestMsgTime();
		}
		return new GroupCount(msgCount, unreadCount, latestTime);
	}
	@Override
	public void trackIncomingMessage(Transaction txn, Message m)
			throws DbException {
		messageTracker.trackIncomingMessage(txn, m);
		Event e = new ConversationMessageTrackedEvent(
				m.getTimestamp(), false,
				clientHelper.getContactId(txn, m.getGroupId()));
		txn.attach(e);
	}
	@Override
	public void trackOutgoingMessage(Transaction txn, Message m)
			throws DbException {
		messageTracker.trackOutgoingMessage(txn, m);
		Event e = new ConversationMessageTrackedEvent(
				m.getTimestamp(), true,
				clientHelper.getContactId(txn, m.getGroupId()));
		txn.attach(e);
	}
	@Override
	public void trackMessage(Transaction txn, GroupId g, long timestamp,
			boolean read)
			throws DbException {
		messageTracker.trackMessage(txn, g, timestamp, read);
		Event e = new ConversationMessageTrackedEvent(
				timestamp, read, clientHelper.getContactId(txn, g));
		txn.attach(e);
	}
	@Override
	public void setReadFlag(GroupId g, MessageId m, boolean read)
			throws DbException {
		db.transaction(false, txn -> setReadFlag(txn, g, m, read));
	}
	@Override
	public void setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException {
		boolean wasRead = messageTracker.setReadFlag(txn, g, m, read);
		if (read && !wasRead) db.startCleanupTimer(txn, m);
	}
	@Override
	public long getTimestampForOutgoingMessage(Transaction txn, ContactId c)
			throws DbException {
		long now = clock.currentTimeMillis();
		GroupCount gc = getGroupCount(txn, c);
		return max(now, gc.getLatestMsgTime() + 1);
	}
	@Override
	public DeletionResult deleteAllMessages(ContactId c) throws DbException {
		return db.transactionWithResult(false, txn ->
				deleteAllMessages(txn, c));
	}
	@Override
	public DeletionResult deleteAllMessages(Transaction txn, ContactId c)
			throws DbException {
		DeletionResult result = new DeletionResult();
		for (ConversationClient client : clients) {
			result.addDeletionResult(client.deleteAllMessages(txn, c));
		}
		return result;
	}
	@Override
	public DeletionResult deleteMessages(ContactId c,
			Collection<MessageId> toDelete) throws DbException {
		return db.transactionWithResult(false, txn ->
				deleteMessages(txn, c, toDelete));
	}
	@Override
	public DeletionResult deleteMessages(Transaction txn, ContactId c,
			Collection<MessageId> toDelete) throws DbException {
		DeletionResult result = new DeletionResult();
		for (ConversationClient client : clients) {
			Set<MessageId> idSet = client.getMessageIds(txn, c);
			idSet.retainAll(toDelete);
			result.addDeletionResult(client.deleteMessages(txn, c, idSet));
		}
		return result;
	}
}
