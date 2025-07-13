package org.nodex.api.sharing.event;

import org.nodex.api.event.Event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Event broadcast when a sharing invitation is received.
 */
@Immutable
@NotNullByDefault
public class SharingInvitationReceivedEvent implements Event {
    
    private final ContactId contactId;
    private final String invitationText;
    
    public SharingInvitationReceivedEvent(ContactId contactId, String invitationText) {
        this.contactId = contactId;
        this.invitationText = invitationText;
    }
    
    public ContactId getContactId() {
        return contactId;
    }
    
    public String getInvitationText() {
        return invitationText;
    }
}
