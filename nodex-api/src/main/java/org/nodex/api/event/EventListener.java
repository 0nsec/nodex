package org.nodex.api.event;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Interface for listening to events.
 */
@NotNullByDefault
public interface EventListener {
    
    /**
     * Called when an event occurs.
     */
    void eventOccurred(Event event);
}
