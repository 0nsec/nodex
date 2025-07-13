package org.nodex.api.transport.mailbox;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MailboxManager {
    
    String CLIENT_ID = "org.nodex.client.mailbox";
    int MAJOR_VERSION = 1;
    int MINOR_VERSION = 0;
    
    void connectToMailbox(Transaction txn, ContactId contactId) throws DbException;
    
    void disconnectFromMailbox(Transaction txn, ContactId contactId) throws DbException;
    
    boolean isConnectedToMailbox(Transaction txn, ContactId contactId) throws DbException;
}
