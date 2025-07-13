package org.nodex.api.client;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;

/**
 * Factory for creating contact groups.
 */
@NotNullByDefault
public interface ContactGroupFactory {
    
    /**
     * Creates a contact group for the given contact.
     */
    Group createContactGroup(ContactId contactId);
    
    /**
     * Creates a contact group with the specified ID.
     */
    Group createContactGroup(ContactId contactId, GroupId groupId);
    
    /**
     * Creates a local group for the given client.
     */
    Group createLocalGroup(org.nodex.api.sync.ClientId clientId, int majorVersion);
    
    /**
     * Returns the group ID for a contact.
     */
    GroupId getContactGroupId(ContactId contactId);
}
