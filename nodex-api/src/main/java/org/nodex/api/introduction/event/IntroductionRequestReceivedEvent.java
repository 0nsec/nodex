package org.nodex.api.introduction.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event fired when an introduction request is received.
 */
@Immutable
@NotNullByDefault
public class IntroductionRequestReceivedEvent extends Event {

    private final ContactId contactId;
    private final String text;

    public IntroductionRequestReceivedEvent(ContactId contactId, String text) {
        this.contactId = contactId;
        this.text = text;
    }

    public ContactId getContactId() {
        return contactId;
    }

    public String getText() {
        return text;
    }
}
