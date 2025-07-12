package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * A group represents a collection of messages and contacts
 */
@Immutable
@NotNullByDefault
public class Group {
    
    /**
     * The maximum length of a group descriptor in bytes.
     */
    public static final int MAX_GROUP_DESCRIPTOR_LENGTH = 1024;
    
    /**
     * The current version of the group format.
     */
    public static final int FORMAT_VERSION = 1;
    
    /**
     * Group visibility levels.
     */
    public enum Visibility {
        /**
         * The group is invisible to contacts.
         */
        INVISIBLE,
        
        /**
         * The group is visible to contacts but not shared.
         */
        VISIBLE,
        
        /**
         * The group is shared with contacts.
         */
        SHARED
    }

    private final GroupId id;
    private final ClientId clientId;
    private final int majorVersion;
    private final byte[] descriptor;

    public Group(GroupId id, ClientId clientId, int majorVersion, byte[] descriptor) {
        if (id == null) throw new IllegalArgumentException("Group ID cannot be null");
        if (clientId == null) throw new IllegalArgumentException("Client ID cannot be null");
        if (descriptor == null) throw new IllegalArgumentException("Descriptor cannot be null");
        if (descriptor.length > MAX_GROUP_DESCRIPTOR_LENGTH) {
            throw new IllegalArgumentException("Descriptor too long");
        }
        this.id = id;
        this.clientId = clientId;
        this.majorVersion = majorVersion;
        this.descriptor = descriptor.clone();
    }

    public GroupId getId() {
        return id;
    }
    
    public ClientId getClientId() {
        return clientId;
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }

    public byte[] getDescriptor() {
        return descriptor.clone();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Group && id.equals(((Group) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Group{id=" + id + ", clientId=" + clientId + ", majorVersion=" + majorVersion + '}';
    }
}
