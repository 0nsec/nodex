package org.nodex.api.versioning;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Visibility;

@NotNullByDefault
public interface ClientVersioningManagerExtensions {
    
    void registerClient(String clientId, int majorVersion, int minorVersion, Object client);
    
    Visibility getClientVisibility(Transaction txn, ContactId contactId, String clientId, int majorVersion) throws DbException;
}
