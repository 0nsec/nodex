package org.briarproject.briar.api.conversation;
import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.sync.Group;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.Message;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.briar.api.client.MessageTracker.GroupCount;
import org.briarproject.briar.api.messaging.MessagingManager;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.Set;
@NotNullByDefault
public interface ConversationManager {
	int DELETE_SESSION_INTRODUCTION_INCOMPLETE = 1;
	int DELETE_SESSION_INVITATION_INCOMPLETE = 1 << 1;
	int DELETE_SESSION_INTRODUCTION_IN_PROGRESS = 1 << 2;
	int DELETE_SESSION_INVITATION_IN_PROGRESS = 1 << 3;
	void registerConversationClient(ConversationClient client);
	Collection<ConversationMessageHeader> getMessageHeaders(ContactId c)
			throws DbException;
	Collection<ConversationMessageHeader> getMessageHeaders(Transaction txn, ContactId c)
			throws DbException;
	GroupCount getGroupCount(ContactId c) throws DbException;
	GroupCount getGroupCount(Transaction txn, ContactId c) throws DbException;
	void trackIncomingMessage(Transaction txn, Message m)
			throws DbException;
	void trackOutgoingMessage(Transaction txn, Message m)
			throws DbException;
	void trackMessage(Transaction txn, GroupId g, long timestamp, boolean read)
			throws DbException;
	void setReadFlag(GroupId g, MessageId m, boolean read)
			throws DbException;
	void setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException;
	long getTimestampForOutgoingMessage(Transaction txn, ContactId c)
			throws DbException;
	DeletionResult deleteAllMessages(ContactId c) throws DbException;
	DeletionResult deleteAllMessages(Transaction txn, ContactId c)
			throws DbException;
	DeletionResult deleteMessages(ContactId c, Collection<MessageId> messageIds)
			throws DbException;
	DeletionResult deleteMessages(Transaction txn, ContactId c,
			Collection<MessageId> messageIds) throws DbException;
	@NotNullByDefault
	interface ConversationClient {
		Group getContactGroup(Contact c);
		Collection<ConversationMessageHeader> getMessageHeaders(Transaction txn,
				ContactId contactId) throws DbException;
		Set<MessageId> getMessageIds(Transaction txn, ContactId contactId)
				throws DbException;
		GroupCount getGroupCount(Transaction txn, ContactId c)
				throws DbException;
		DeletionResult deleteAllMessages(Transaction txn,
				ContactId c) throws DbException;
		DeletionResult deleteMessages(Transaction txn, ContactId c,
				Set<MessageId> messageIds) throws DbException;
	}
}