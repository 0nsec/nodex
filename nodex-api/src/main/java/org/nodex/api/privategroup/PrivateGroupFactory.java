package org.nodex.api.privategroup;

import org.nodex.api.FormatException;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;

/**
 * Factory for creating private groups.
 */
@NotNullByDefault
public interface PrivateGroupFactory {

    /**
     * Creates a private group.
     */
    PrivateGroup createPrivateGroup(String name, LocalAuthor creator);

    /**
     * Creates a private group from a group descriptor.
     */
    PrivateGroup createPrivateGroup(Group group) throws FormatException;
}
