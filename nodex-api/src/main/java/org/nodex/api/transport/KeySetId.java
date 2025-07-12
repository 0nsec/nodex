package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Represents a key set identifier for transport connections.
 */
@Immutable
@NotNullByDefault
public class KeySetId {
    
    private final byte[] id;
    
    public KeySetId(byte[] id) {
        this.id = Arrays.copyOf(id, id.length);
    }
    
    public byte[] getBytes() {
        return Arrays.copyOf(id, id.length);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeySetId keySetId = (KeySetId) o;
        return Arrays.equals(id, keySetId.id);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }
}
