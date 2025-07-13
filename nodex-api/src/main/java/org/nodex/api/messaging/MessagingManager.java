


import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.attachment.FileTooBigException;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.Contact;
import org.nodex.api.messaging.PrivateMessageHeader;
import java.util.Collection;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageFormat;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MessagingManager extends ConversationClient {
    ClientId CLIENT_ID = new ClientId("org.nodex.client.messaging");
    int MAJOR_VERSION = 1;
    int MINOR_VERSION = 0;

    void addLocalMessage(PrivateMessage m) throws DbException;
    void addLocalMessage(Transaction txn, PrivateMessage m) throws DbException;
    AttachmentHeader addLocalAttachment(GroupId groupId, long timestamp,
            String contentType, InputStream is) throws DbException, IOException, FileTooBigException;
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
    // Methods required by MessagingManagerImpl
    // Change return type to match ConversationClient to avoid clash
    org.nodex.api.sync.Group getContactGroup(Contact contact);
    Collection<PrivateMessageHeader> getMessageHeaders(ContactId contactId) throws DbException;
    void setReadFlag(MessageId messageId, boolean read) throws DbException;
    long getTimestamp(MessageId messageId) throws DbException;
// removed extra closing brace
}
