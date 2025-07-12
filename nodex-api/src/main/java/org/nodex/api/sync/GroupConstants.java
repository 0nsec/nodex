package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for group functionality.
 */
@NotNullByDefault
public class GroupConstants {

    /**
     * Maximum length for group names.
     */
    public static final int MAX_GROUP_NAME_LENGTH = 100;

    /**
     * Maximum length for group descriptions.
     */
    public static final int MAX_GROUP_DESCRIPTION_LENGTH = 500;

    /**
     * Maximum number of members in a group.
     */
    public static final int MAX_GROUP_MEMBERS = 100;

    private GroupConstants() {
        // Prevent instantiation
    }
}
