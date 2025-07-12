package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Factory for creating Group objects.
 */
@NotNullByDefault
public interface GroupFactory {
    
    /**
     * Creates a new group with the specified parameters.
     * 
     * @param clientId The client ID for the group
     * @param majorVersion The major version of the group
     * @param descriptor The group descriptor
     * @return A new Group instance
     */
    Group createGroup(ClientId clientId, int majorVersion, byte[] descriptor);
    
    /**
     * Creates a new group with the specified parameters and visibility.
     * 
     * @param clientId The client ID for the group
     * @param majorVersion The major version of the group
     * @param descriptor The group descriptor
     * @param visibility The visibility setting for the group
     * @return A new Group instance
     */
    Group createGroup(ClientId clientId, int majorVersion, byte[] descriptor, Group.Visibility visibility);
}
