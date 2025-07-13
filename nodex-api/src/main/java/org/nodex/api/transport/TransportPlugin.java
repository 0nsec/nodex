package org.nodex.api.transport;

import org.nodex.api.plugin.Plugin;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.Collection;

@NotNullByDefault
public interface TransportPlugin extends Plugin {
    
    /**
     * Get the transport ID for this plugin.
     */
    TransportId getId();
    
    /**
     * Generate transport keys for this plugin.
     */
    TransportKeys generateKeys();
    
    /**
     * Check if we can connect to a contact.
     */
    boolean shouldConnect(ContactId contactId);
    
    /**
     * Get the maximum latency for this transport in milliseconds.
     */
    long getMaxLatency();
}
