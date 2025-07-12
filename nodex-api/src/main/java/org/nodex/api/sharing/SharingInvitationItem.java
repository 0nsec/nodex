package org.nodex.api.sharing;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Represents an invitation item for sharing.
 */
@Immutable
@NotNullByDefault
public class SharingInvitationItem {
    
    private final ContactId contactId;
    private final String text;
    private final long timestamp;
    private final boolean accepted;
    
    public SharingInvitationItem(ContactId contactId, String text, long timestamp, boolean accepted) {
        this.contactId = contactId;
        this.text = text;
        this.timestamp = timestamp;
        this.accepted = accepted;
    }
    
    public ContactId getContactId() {
        return contactId;
    }
    
    public String getText() {
        return text;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharingInvitationItem)) return false;
        SharingInvitationItem that = (SharingInvitationItem) o;
        return timestamp == that.timestamp &&
               accepted == that.accepted &&
               contactId.equals(that.contactId) &&
               text.equals(that.text);
    }
    
    @Override
    public int hashCode() {
        int result = contactId.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + Boolean.hashCode(accepted);
        return result;
    }
}
