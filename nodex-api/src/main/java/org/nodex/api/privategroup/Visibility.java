package org.nodex.api.privategroup;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Visibility of a private group member.
 */
@NotNullByDefault
public enum Visibility {
    /**
     * The member is not visible.
     */
    INVISIBLE,
    
    /**
     * The member is visible.
     */
    VISIBLE,
    
    /**
     * The member was revealed by us.
     */
    REVEALED_BY_US,
    
    /**
     * The member was revealed by a contact.
     */
    REVEALED_BY_CONTACT
}
