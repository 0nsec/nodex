package org.briarproject.briar.api.sharing;
import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.briar.api.client.ProtocolStateException;
import org.briarproject.briar.api.client.SessionId;
import org.briarproject.briar.api.conversation.ConversationManager.ConversationClient;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
@NotNullByDefault
public interface SharingManager<S extends Shareable>
		extends ConversationClient {
	enum SharingStatus {
		SHAREABLE,
		INVITE_SENT,
		INVITE_RECEIVED,
		SHARING,
		NOT_SUPPORTED,
		ERROR
	}
	void sendInvitation(GroupId shareableId, ContactId contactId,
			@Nullable String text) throws DbException;
	void sendInvitation(Transaction txn, GroupId shareableId,
			ContactId contactId, @Nullable String text) throws DbException;
	void respondToInvitation(S s, Contact c, boolean accept)
			throws DbException;
	void respondToInvitation(Transaction txn, S s, Contact c, boolean accept)
			throws DbException;
	void respondToInvitation(ContactId c, SessionId id, boolean accept)
			throws DbException;
	void respondToInvitation(Transaction txn, ContactId c, SessionId id,
			boolean accept) throws DbException;
	Collection<SharingInvitationItem> getInvitations() throws DbException;
	Collection<SharingInvitationItem> getInvitations(Transaction txn)
			throws DbException;
	Collection<Contact> getSharedWith(GroupId g) throws DbException;
	Collection<Contact> getSharedWith(Transaction txn, GroupId g)
			throws DbException;
	SharingStatus getSharingStatus(GroupId g, Contact c) throws DbException;
	SharingStatus getSharingStatus(Transaction txn, GroupId g, Contact c)
			throws DbException;
}