package org.nodex.api.transport;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

import java.util.Collection;

/**
 * Manages transport connections and routing.
 */
@NotNullByDefault
public interface TransportManager extends Service {
    
    /**
     * Get all active transport plugins.
     */
    Collection<TransportPlugin> getTransportPlugins();
    
    /**
     * Send data to a contact via the best available transport.
     */
    void sendData(ContactId contactId, byte[] data);
    
    /**
     * Check if a contact is reachable via any transport.
     */
    boolean isReachable(ContactId contactId);
}
