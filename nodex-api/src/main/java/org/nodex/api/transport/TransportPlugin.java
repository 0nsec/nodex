package org.nodex.api.transport;

import org.nodex.api.plugin.TransportId;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * Interface for transport plugins.
 */
@NotNullByDefault
public interface TransportPlugin {
    
    /**
     * Returns the transport ID.
     */
    TransportId getId();
    
    /**
     * Returns the maximum frame length for this transport.
     */
    int getMaxFrameLength();
    
    /**
     * Returns the maximum latency in milliseconds.
     */
    int getMaxLatency();
    
    /**
     * Returns the maximum idle time in milliseconds.
     */
    int getMaxIdleTime();
    
    /**
     * Returns true if this transport should be polled.
     */
    boolean shouldPoll();
    
    /**
     * Returns the polling interval in milliseconds.
     */
    int getPollingInterval();
    
    /**
     * Returns the supported transport properties.
     */
    Collection<TransportProperties> getSupportedProperties();
    
    /**
     * Generates transport-specific keys.
     */
    TransportKeys generateKeys();
    
    /**
     * Returns true if this transport supports key agreement.
     */
    boolean supportsKeyAgreement();
    
    /**
     * Returns true if this transport supports rendezvous connections.
     */
    boolean supportsRendezvous();
    
    /**
     * Poll for connections with the given contacts.
     */
    void poll(Collection<ContactId> connectedContacts);
}
