package org.nodex.core.api.sync;

import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * A unique identifier for a message.
 */
@Immutable
@NotNullByDefault
public class MessageId {
    private final byte[] id;
    
    public MessageId(byte[] id) {
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
}
