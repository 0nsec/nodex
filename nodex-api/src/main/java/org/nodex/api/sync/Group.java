package org.nodex.api.sync;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A group represents a collection of messages and contacts
 */
@NotNullByDefault
public class Group {
    private final GroupId id;
    private final byte[] descriptor;
    private final long version;

    public Group(GroupId id, byte[] descriptor, long version) {
        if (id == null) throw new IllegalArgumentException("Group ID cannot be null");
        if (descriptor == null) throw new IllegalArgumentException("Descriptor cannot be null");
        this.id = id;
        this.descriptor = descriptor.clone();
        this.version = version;
    }

    public GroupId getId() {
        return id;
    }

    public byte[] getDescriptor() {
        return descriptor.clone();
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Group{id=" + id + ", version=" + version + '}';
    }
}
