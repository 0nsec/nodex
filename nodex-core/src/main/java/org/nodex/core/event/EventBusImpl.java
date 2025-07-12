package org.nodex.core.event;

import org.nodex.api.event.EventBus;
import org.nodex.api.event.Event;
import org.nodex.api.event.EventListener;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Implementation of EventBus for publishing and subscribing to events.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class EventBusImpl implements EventBus {

    private static final Logger LOG = Logger.getLogger(EventBusImpl.class.getName());

    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListener>> listeners;
    private final Executor executor;

    @Inject
    public EventBusImpl() {
        this.listeners = new ConcurrentHashMap<>();
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "EventBus");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void broadcast(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        CopyOnWriteArrayList<EventListener> eventListeners = listeners.get(eventClass);
        
        if (eventListeners != null && !eventListeners.isEmpty()) {
            LOG.fine("Broadcasting event: " + eventClass.getSimpleName() + 
                    " to " + eventListeners.size() + " listeners");
            
            for (EventListener listener : eventListeners) {
                executor.execute(() -> {
                    try {
                        listener.eventOccurred(event);
                    } catch (Exception e) {
                        LOG.warning("Error in event listener: " + e.getMessage());
                    }
                });
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(Class<T> eventClass, EventListener listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
        LOG.fine("Added listener for event: " + eventClass.getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void removeListener(Class<T> eventClass, EventListener listener) {
        CopyOnWriteArrayList<EventListener> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventClass);
            }
            LOG.fine("Removed listener for event: " + eventClass.getSimpleName());
        }
    }
}
