package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.contact.ContactId;
import org.nodex.api.lifecycle.Service;

import java.util.Collection;

/**
 * Interface for transport plugins that handle communication over specific protocols.
 */
@NotNullByDefault
public interface TransportPlugin extends Service {
    
    /**
     * Get the unique identifier for this transport.
     */
    TransportId getId();
    
    /**
     * Get the maximum latency in milliseconds for this transport.
     */
    int getMaxLatency();
    
    /**
     * Get the maximum message size in bytes for this transport.
     */
    int getMaxIdleTime();
    
    /**
     * Check if this transport should be polled for incoming connections.
     */
    boolean shouldPoll();
    
    /**
     * Check if this transport supports key agreement.
     */
    boolean supportsKeyAgreement();
    
    /**
     * Start the transport and begin listening for connections.
     */
    void startTransport() throws TransportException;
    
    /**
     * Stop the transport and close all connections.
     */
    void stopTransport() throws TransportException;
    
    /**
     * Check if the transport is running.
     */
    boolean isRunning();
    
    /**
     * Create an outgoing connection to the specified contact.
     */
    boolean createConnection(ContactId contactId);
}
