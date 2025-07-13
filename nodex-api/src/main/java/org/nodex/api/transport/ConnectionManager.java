package org.nodex.api.transport;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

import java.util.Collection;

/**
 * Manages connections for a specific transport.
 */
@NotNullByDefault
public interface ConnectionManager extends Service {
    
    /**
     * Attempt to connect to a contact.
     */
    void connect(ContactId contactId);
    
    /**
     * Disconnect from a contact.
     */
    void disconnect(ContactId contactId);
    
    /**
     * Get all active connections.
     */
    Collection<ContactId> getConnectedContacts();
    
    /**
     * Check if connected to a specific contact.
     */
    boolean isConnected(ContactId contactId);
}
