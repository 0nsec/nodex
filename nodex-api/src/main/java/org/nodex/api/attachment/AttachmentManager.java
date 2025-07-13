package org.nodex.api.attachment;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;

import java.io.InputStream;

@NotNullByDefault
public interface AttachmentManager {
    
    String CLIENT_ID = "org.nodex.client.attachment";
    int MAJOR_VERSION = 1;
    int MINOR_VERSION = 0;
    
    void storeAttachment(Transaction txn, ContactId contactId, MessageId messageId, 
                        String contentType, InputStream data) throws DbException, FileTooBigException;
    
    InputStream getAttachment(Transaction txn, MessageId messageId) throws DbException;
    
    void deleteAttachment(Transaction txn, MessageId messageId) throws DbException;
    
    boolean hasAttachment(Transaction txn, MessageId messageId) throws DbException;
}
