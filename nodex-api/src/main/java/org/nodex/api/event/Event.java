package org.nodex.api.event;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base class for all events in the system
 */
@NotNullByDefault
public abstract class Event {
    private final long timestamp;

    public Event() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{timestamp=" + timestamp + '}';
    }
}
