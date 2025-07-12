package org.nodex.api.introduction.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event that is broadcast when an introduction is aborted.
 */
@Immutable
@NotNullByDefault
public class IntroductionAbortedEvent extends Event {

    private final ContactId contactId;
    private final String reason;

    public IntroductionAbortedEvent(ContactId contactId, String reason) {
        this.contactId = contactId;
        this.reason = reason;
    }

    /**
     * Returns the ID of the contact.
     */
    public ContactId getContactId() {
        return contactId;
    }

    /**
     * Returns the reason for the abort.
     */
    public String getReason() {
        return reason;
    }
}
