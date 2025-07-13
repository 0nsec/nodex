package org.nodex.api.connection;

import org.nodex.api.contact.ContactId;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * Registry for active connections.
 */
@NotNullByDefault
public interface ConnectionRegistry {
    
    /**
     * Register a new connection.
     */
    void registerConnection(ContactId contactId, TransportId transportId);
    
    /**
     * Unregister a connection.
     */
    void unregisterConnection(ContactId contactId, TransportId transportId);
    
    /**
     * Get all contacts with active connections.
     */
    Collection<ContactId> getConnectedContacts();
    
    /**
     * Check if a contact is connected via any transport.
     */
    boolean isConnected(ContactId contactId);
}
