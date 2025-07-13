package org.nodex.api.contact;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ContactId {
    private final byte[] id;

    public ContactId(byte[] id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("ContactId cannot be null or empty");
        }
        this.id = id.clone();
    }

    public ContactId(int id) {
        this.id = ByteBuffer.allocate(4).putInt(id).array();
    }

    public byte[] getBytes() {
        return id.clone();
    }

    public int getInt() {
        if (id.length < 4) {
            throw new IllegalStateException("ContactId does not contain a valid int");
        }
        return ByteBuffer.wrap(id).getInt();
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
