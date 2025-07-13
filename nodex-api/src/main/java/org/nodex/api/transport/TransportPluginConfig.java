package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.plugin.TransportId;

@NotNullByDefault
public class TransportPluginConfig {
    
    private final TransportId transportId;
    private final boolean enabled;
    
    public TransportPluginConfig(TransportId transportId, boolean enabled) {
        this.transportId = transportId;
        this.enabled = enabled;
    }
    
    public TransportId getTransportId() {
        return transportId;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
