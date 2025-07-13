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
}
