package org.nodex.api.db;

import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.Visibility;

import java.util.Collection;
import java.util.Collections;

@NotNullByDefault
public abstract class DatabaseComponentImpl implements DatabaseComponent {
    
    public <R, E extends Exception> R transactionWithResult(boolean readOnly, DbCallable<R, E> task) throws DbException, E {
        // Implementation would depend on the actual database backend
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public void startCleanupTimer(Transaction txn, MessageId messageId) throws DbException {
        // Implementation for cleanup timer
    }
    
    public Contact getContact(Transaction txn, ContactId contactId) throws DbException {
        // Implementation to get contact
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    public Collection<Contact> getContacts(Transaction txn) throws DbException {
        // Implementation to get all contacts
        return Collections.emptyList();
    }
    
    public boolean containsGroup(Transaction txn, GroupId groupId) throws DbException {
        // Implementation to check group existence
        return false;
    }
    
    public void addGroup(Transaction txn, Group group) throws DbException {
        // Implementation to add group
    }
    
    public void removeGroup(Transaction txn, Group group) throws DbException {
        // Implementation to remove group
    }
    
    public void setGroupVisibility(Transaction txn, ContactId contactId, GroupId groupId, Visibility visibility) throws DbException {
        // Implementation to set group visibility
    }
    
    public void stopCleanupTimer(Transaction txn, MessageId messageId) throws DbException {
        // Implementation to stop cleanup timer
    }
    
    public void setCleanupTimerDuration(Transaction txn, MessageId messageId, long duration) throws DbException {
        // Implementation to set cleanup timer duration
    }
}
