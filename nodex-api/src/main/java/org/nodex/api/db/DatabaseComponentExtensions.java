package org.nodex.api.db;

import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.Visibility;

import java.util.Collection;

@NotNullByDefault
public interface DatabaseComponentExtensions {
    
    <R, E extends Exception> R transactionWithResult(boolean readOnly, DbCallable<R, E> task) throws DbException, E;
    
    void startCleanupTimer(Transaction txn, MessageId messageId) throws DbException;
    
    Contact getContact(Transaction txn, ContactId contactId) throws DbException;
    
    Collection<Contact> getContacts(Transaction txn) throws DbException;
    
    boolean containsGroup(Transaction txn, GroupId groupId) throws DbException;
    
    void addGroup(Transaction txn, Group group) throws DbException;
    
    void removeGroup(Transaction txn, Group group) throws DbException;
    
    void setGroupVisibility(Transaction txn, ContactId contactId, GroupId groupId, Visibility visibility) throws DbException;
}
