package org.nodex.api.messaging;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.attachment.FileTooBigException;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
@NotNullByDefault
public interface MessagingManager extends ConversationClient {
	ClientId CLIENT_ID = new ClientId("org.nodex.messaging");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 3;
	void addLocalMessage(PrivateMessage m) throws DbException;
	void addLocalMessage(Transaction txn, PrivateMessage m) throws DbException;
	AttachmentHeader addLocalAttachment(GroupId groupId, long timestamp,
			String contentType, InputStream is) throws DbException, IOException;
	void removeAttachment(AttachmentHeader header) throws DbException;
	ContactId getContactId(GroupId g) throws DbException;
	GroupId getConversationId(ContactId c) throws DbException;
	GroupId getConversationId(Transaction txn, ContactId c) throws DbException;
	@Nullable
	String getMessageText(MessageId m) throws DbException;
	@Nullable
	String getMessageText(Transaction txn, MessageId m) throws DbException;
	PrivateMessageFormat getContactMessageFormat(Transaction txn, ContactId c)
			throws DbException;
}