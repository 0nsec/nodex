package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class UniqueId {
    
    public static final int LENGTH = 32; // 32 bytes = 256 bits
    
    private final byte[] id;
    
    public UniqueId(byte[] id) {
        if (id.length != LENGTH) {
            throw new IllegalArgumentException("ID must be " + LENGTH + " bytes");
        }
        this.id = id.clone();
    }
    
    public byte[] getBytes() {
        return id.clone();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UniqueId)) return false;
        UniqueId other = (UniqueId) obj;
        return java.util.Arrays.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(id);
    }
}
