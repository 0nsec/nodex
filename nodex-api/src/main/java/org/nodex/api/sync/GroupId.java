package org.nodex.api.sync;

import java.util.Arrays;

public class GroupId {
    private final byte[] id;

    public GroupId(byte[] id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("GroupId cannot be null or empty");
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
        GroupId groupId = (GroupId) o;
        return Arrays.equals(id, groupId.id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public String toString() {
        return "GroupId{" + Arrays.toString(id) + '}';
    }
}
