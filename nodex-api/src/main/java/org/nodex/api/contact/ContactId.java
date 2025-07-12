package org.nodex.api.contact;

import java.util.Arrays;

public class ContactId {
    private final byte[] id;

    public ContactId(byte[] id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("ContactId cannot be null or empty");
        }
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

    @Override
    public String toString() {
        return "ContactId{" + Arrays.toString(id) + '}';
    }
}
