package org.nodex.api.privategroup.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when a contact relationship is revealed.
 */
@Immutable
@NotNullByDefault
public class ContactRelationshipRevealedEvent extends Event {

    private final ContactId contactId;

    public ContactRelationshipRevealedEvent(ContactId contactId) {
        this.contactId = contactId;
    }

    public ContactId getContactId() {
        return contactId;
    }
}
