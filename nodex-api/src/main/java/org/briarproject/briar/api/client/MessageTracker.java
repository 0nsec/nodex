package org.nodex.api.client;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
public interface MessageTracker {
	void initializeGroupCount(Transaction txn, GroupId g) throws DbException;
	GroupCount getGroupCount(GroupId g) throws DbException;
	GroupCount getGroupCount(Transaction txn, GroupId g) throws DbException;
	void trackIncomingMessage(Transaction txn, Message m) throws DbException;
	void trackOutgoingMessage(Transaction txn, Message m) throws DbException;
	void trackMessage(Transaction txn, GroupId g, long timestamp, boolean read)
			throws DbException;
	@Nullable
	MessageId loadStoredMessageId(GroupId g) throws DbException;
	void storeMessageId(GroupId g, MessageId m) throws DbException;
	boolean setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException;
	void resetGroupCount(Transaction txn, GroupId g, int msgCount,
			int unreadCount) throws DbException;
	class GroupCount {
		private final int msgCount, unreadCount;
		private final long latestMsgTime;
		public GroupCount(int msgCount, int unreadCount, long latestMsgTime) {
			this.msgCount = msgCount;
			this.unreadCount = unreadCount;
			this.latestMsgTime = latestMsgTime;
		}
		public int getMsgCount() {
			return msgCount;
		}
		public int getUnreadCount() {
			return unreadCount;
		}
		public long getLatestMsgTime() {
			return latestMsgTime;
		}
	}
}