package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Arrays;

/**
 * Metadata associated with a message or group.
 */
@NotNullByDefault
public class Metadata {
    
    private final byte[] data;
    
    public Metadata(byte[] data) {
        this.data = data.clone();
    }
    
    public byte[] getData() {
        return data.clone();
    }
    
    public int getLength() {
        return data.length;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Metadata)) return false;
        Metadata other = (Metadata) obj;
        return Arrays.equals(data, other.data);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
    
    @Override
    public String toString() {
        return "Metadata[" + data.length + " bytes]";
    }
}
