package org.nodex.api.cleanup.event;

import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class CleanupTimerStartedEvent extends Event {
    
    private final long startTime;
    
    public CleanupTimerStartedEvent(long startTime) {
        this.startTime = startTime;
    }
    
    public long getStartTime() {
        return startTime;
    }
}
