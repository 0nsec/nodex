package org.nodex.api.plugin.event;

import org.nodex.api.plugin.TransportId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event that signals a transport plugin has become active.
 */
@Immutable
@NotNullByDefault
public class TransportActiveEvent extends Event {
    
    private final TransportId transportId;
    
    public TransportActiveEvent(TransportId transportId) {
        this.transportId = transportId;
    }
    
    public TransportId getTransportId() {
        return transportId;
    }
}
