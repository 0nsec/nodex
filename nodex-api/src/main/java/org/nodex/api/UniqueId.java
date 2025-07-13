package org.nodex.api;

import java.util.Arrays;

/**
 * Base class for unique identifiers
 */
public abstract class UniqueId {
    
    /**
     * Standard length for unique identifiers.
     */
    public static final int LENGTH = 32;
    
    protected final byte[] id;

    protected UniqueId(byte[] id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("ID cannot be null or empty");
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
        UniqueId uniqueId = (UniqueId) o;
        return Arrays.equals(id, uniqueId.id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + Arrays.toString(id) + '}';
    }
}
