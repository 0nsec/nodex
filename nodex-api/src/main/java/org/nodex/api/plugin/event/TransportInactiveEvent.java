package org.nodex.api.plugin.event;

import org.nodex.api.plugin.TransportId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event that signals a transport plugin has become inactive.
 */
@Immutable
@NotNullByDefault
public class TransportInactiveEvent extends Event {
    
    private final TransportId transportId;
    
    public TransportInactiveEvent(TransportId transportId) {
        this.transportId = transportId;
    }
    
    public TransportId getTransportId() {
        return transportId;
    }
}
