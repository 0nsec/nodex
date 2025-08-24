package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents the visibility of content to a contact.
 */
@NotNullByDefault
public enum Visibility {
    /**
     * Content is visible to the contact.
     */
    VISIBLE,
    
    /**
     * Content is not visible to the contact.
     */
    INVISIBLE,
    
    /**
     * Content visibility is shared with the contact.
     */
    SHARED
    ;

    // Minimal helper used by core code (Visibility.min(a,b))
    public static Visibility min(Visibility a, Visibility b) {
        // Define ordering: INVISIBLE < VISIBLE < SHARED
        return (rank(a) <= rank(b)) ? a : b;
    }
    private static int rank(Visibility v) {
        switch (v) {
            case INVISIBLE: return 0;
            case VISIBLE: return 1;
            case SHARED: return 2;
            default: return 0;
        }
    }
}
