package org.nodex.api.introduction.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when an introduction response is received.
 */
@Immutable
@NotNullByDefault
public class IntroductionResponseReceivedEvent extends Event {

    private final ContactId contactId;
    private final boolean accepted;

    public IntroductionResponseReceivedEvent(ContactId contactId, boolean accepted) {
        this.contactId = contactId;
        this.accepted = accepted;
    }

    public ContactId getContactId() {
        return contactId;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
