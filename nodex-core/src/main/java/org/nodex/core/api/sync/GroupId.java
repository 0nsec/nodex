package org.nodex.core.api.sync;

import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * A unique identifier for a group.
 */
@Immutable
@NotNullByDefault
public class GroupId {
    private final byte[] id;
    
    public GroupId(byte[] id) {
        this.id = id.clone();
    }
    
    public byte[] getBytes() {
        return id.clone();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupId groupId = (GroupId) o;
        return Arrays.equals(id, groupId.id);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }
}
