package org.nodex.core.api.contact;

import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * A unique identifier for a contact.
 */
@Immutable
@NotNullByDefault
public class ContactId {
    private final byte[] id;
    
    public ContactId(byte[] id) {
        this.id = id.clone();
    }
    
    public byte[] getBytes() {
        return id.clone();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactId contactId = (ContactId) o;
        return Arrays.equals(id, contactId.id);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }
}
