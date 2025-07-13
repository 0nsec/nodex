package org.nodex.client;
import org.nodex.api.client.BdfIncomingMessageHook;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ConversationClientImpl extends BdfIncomingMessageHook
		implements ConversationClient {
		
	private static final ClientId CLIENT_ID = new ClientId("org.nodex.conversation");
	private static final int MAJOR_VERSION = 0;
	
	protected final MessageTracker messageTracker;
	protected ConversationClientImpl(DatabaseComponent db,
			ClientHelper clientHelper, MetadataParser metadataParser,
			MessageTracker messageTracker) {
		super(CLIENT_ID, MAJOR_VERSION);
		this.db = db;
		this.clientHelper = clientHelper;
		this.metadataParser = metadataParser;
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
