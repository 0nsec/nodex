package org.nodex.api.properties;

import org.nodex.api.plugin.TransportId;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Map;

/**
 * Manager for transport properties.
 */
@NotNullByDefault
public interface TransportPropertyManager {
    
    /**
     * Returns the properties for a specific transport.
     */
    TransportProperties getProperties(TransportId transportId);
    
    /**
     * Sets the properties for a specific transport.
     */
    void setProperties(TransportId transportId, TransportProperties properties);
    
    /**
     * Removes properties for a specific transport.
     */
    void removeProperties(TransportId transportId);
    
    /**
     * Adds remote properties for a contact.
     */
    void addRemoteProperties(Transaction txn, ContactId contactId, 
            Map<TransportId, TransportProperties> properties) throws DbException;
}
