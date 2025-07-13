package org.nodex.api.sharing.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class ContactLeftShareableEvent extends Event {
    
    private final ContactId contactId;
    
    public ContactLeftShareableEvent(ContactId contactId) {
        this.contactId = contactId;
    }
    
    public ContactId getContactId() {
        return contactId;
    }
}
