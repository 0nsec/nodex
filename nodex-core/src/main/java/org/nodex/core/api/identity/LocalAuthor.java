package org.nodex.core.api.identity;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Represents a local author/user.
 */
@NotNullByDefault
public interface LocalAuthor {
    String getName();
    byte[] getPublicKey();
}
