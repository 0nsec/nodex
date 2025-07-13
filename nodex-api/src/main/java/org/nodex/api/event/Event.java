package org.nodex.api.event;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public abstract class Event {
    
    private final long timestamp;
    
    protected Event() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
