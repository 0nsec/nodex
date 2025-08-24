package org.nodex.api.event;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Interface for publishing and subscribing to events.
 */
@NotNullByDefault
public interface EventBus {
    
    /**
     * Broadcast an event to all registered listeners.
     */
    void broadcast(Event event);
    
    /**
     * Add a listener for a specific event type.
     */
    <T extends Event> void addListener(Class<T> eventClass, EventListener listener);

    // Legacy wildcard subscription (no-op default)
    default void addListener(EventListener listener) {}
    
    /**
     * Remove a listener for a specific event type.
     */
    <T extends Event> void removeListener(Class<T> eventClass, EventListener listener);

    // Legacy wildcard unsubscription (no-op default)
    default void removeListener(EventListener listener) {}
}
