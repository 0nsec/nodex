package org.nodex.api.sync;

import java.util.Arrays;
import java.util.Objects;

public class MessageId {
    public static final int LENGTH = 32;
    
    private final byte[] id;

    public MessageId(byte[] id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("MessageId cannot be null or empty");
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
        MessageId messageId = (MessageId) o;
        return Arrays.equals(id, messageId.id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public String toString() {
        return "MessageId{" + Arrays.toString(id) + '}';
    }
}
