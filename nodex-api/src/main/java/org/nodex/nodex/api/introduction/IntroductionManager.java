package org.nodex.api.introduction;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.sync.ClientId;
import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationManager.ConversationClient;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
public interface IntroductionManager extends ConversationClient {
	ClientId CLIENT_ID = new ClientId("org.nodex.introduction");
	int MAJOR_VERSION = 1;
	boolean canIntroduce(Contact c1, Contact c2) throws DbException;
	boolean canIntroduce(Transaction txn, Contact c1, Contact c2)
			throws DbException;
	int MINOR_VERSION = 1;
	void makeIntroduction(Contact c1, Contact c2, @Nullable String text)
			throws DbException;
	void makeIntroduction(Transaction txn, Contact c1, Contact c2,
			@Nullable String text) throws DbException;
	void respondToIntroduction(ContactId contactId, SessionId sessionId,
			boolean accept) throws DbException;
	void respondToIntroduction(Transaction txn, ContactId contactId,
			SessionId sessionId, boolean accept) throws DbException;
}