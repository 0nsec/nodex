package org.nodex.client;
import org.nodex.core.api.client.BdfIncomingMessageHook;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.data.MetadataParser;
import org.nodex.core.api.db.DatabaseComponent;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ConversationClientImpl extends BdfIncomingMessageHook
		implements ConversationClient {
	protected final MessageTracker messageTracker;
	protected ConversationClientImpl(DatabaseComponent db,
			ClientHelper clientHelper, MetadataParser metadataParser,
			MessageTracker messageTracker) {
		super(db, clientHelper, metadataParser);
		this.messageTracker = messageTracker;
	}
	@Override
	public GroupCount getGroupCount(Transaction txn, ContactId contactId)
			throws DbException {
		Contact contact = db.getContact(txn, contactId);
		GroupId groupId = getContactGroup(contact).getId();
		return messageTracker.getGroupCount(txn, groupId);
	}
}