package org.nodex.api.client;

import org.nodex.api.FormatException;
import org.nodex.api.contact.ContactId;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;

@NotNullByDefault
public interface ClientHelperExtensions {
    
    ContactId getContactId(Transaction txn, GroupId groupId) throws DbException;
    
    void setContactId(Transaction txn, GroupId groupId, ContactId contactId) throws DbException;
    
    Message createMessage(GroupId groupId, long timestamp, BdfList body) throws FormatException;
    
    Message getMessage(Transaction txn, MessageId messageId) throws DbException;
    
    BdfList toList(Message message) throws FormatException;
}
