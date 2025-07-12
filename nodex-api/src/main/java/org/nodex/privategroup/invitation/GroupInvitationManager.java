package org.nodex.api.privategroup.invitation;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.client.ProtocolStateException;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.sharing.SharingManager.SharingStatus;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import javax.annotation.Nullable;
@NotNullByDefault
public interface GroupInvitationManager extends ConversationClient {
	ClientId CLIENT_ID =
			new ClientId("org.nodex.privategroup.invitation");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 1;
	void sendInvitation(GroupId g, ContactId c, @Nullable String text,
			long timestamp, byte[] signature, long autoDeleteTimer)
			throws DbException;
	void sendInvitation(Transaction txn, GroupId g, ContactId c,
			@Nullable String text, long timestamp, byte[] signature,
			long autoDeleteTimer) throws DbException;
	void respondToInvitation(ContactId c, PrivateGroup g, boolean accept)
			throws DbException;
	void respondToInvitation(Transaction txn, ContactId c, PrivateGroup g,
			boolean accept) throws DbException;
	void respondToInvitation(ContactId c, SessionId s, boolean accept)
			throws DbException;
	void respondToInvitation(Transaction txn, ContactId c, SessionId s,
			boolean accept) throws DbException;
	void revealRelationship(ContactId c, GroupId g) throws DbException;
	void revealRelationship(Transaction txn, ContactId c, GroupId g)
			throws DbException;
	Collection<GroupInvitationItem> getInvitations() throws DbException;
	Collection<GroupInvitationItem> getInvitations(Transaction txn)
			throws DbException;
	SharingStatus getSharingStatus(Contact c, GroupId g) throws DbException;
	SharingStatus getSharingStatus(Transaction txn, Contact c, GroupId g)
			throws DbException;
}