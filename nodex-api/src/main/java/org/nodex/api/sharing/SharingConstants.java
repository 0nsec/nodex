package org.nodex.api.sharing;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for sharing functionality.
 */
@NotNullByDefault
public class SharingConstants {
    
    /**
     * Maximum length for invitation text.
     */
    public static final int MAX_INVITATION_TEXT_LENGTH = 500;
    
    /**
     * Maximum length for sharing message.
     */
    public static final int MAX_SHARING_MESSAGE_LENGTH = 1000;
    
    /**
     * Maximum number of items that can be shared at once.
     */
    public static final int MAX_SHARING_BATCH_SIZE = 100;
    
    private SharingConstants() {
        // Utility class
    }
}
