package org.nodex.api.db;

import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class EventAction implements CommitAction {
    
    private final Event event;
    
    public EventAction(Event event) {
        this.event = event;
    }
    
    @Override
    public void run() {
        // Event will be fired by the transaction manager
    }
    
    public Event getEvent() {
        return event;
    }
}
