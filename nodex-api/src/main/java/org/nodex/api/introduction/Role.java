package org.nodex.api.introduction;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Roles in the introduction protocol.
 */
@NotNullByDefault
public enum Role {
    /**
     * The contact who initiates the introduction.
     */
    INTRODUCER,
    
    /**
     * The contact who is being introduced.
     */
    INTRODUCEE
}
