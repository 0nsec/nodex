package org.nodex.api.privategroup;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for private groups.
 */
@NotNullByDefault
public class PrivateGroupConstants {
    
    public static final int GROUP_SALT_LENGTH = 32;
    public static final int MAX_GROUP_NAME_LENGTH = 100;
    public static final int MAX_GROUP_POST_TEXT_LENGTH = 1000;
    public static final int MAX_GROUP_INVITATION_TEXT_LENGTH = 500;
    
    private PrivateGroupConstants() {
        // Utility class
    }
}
