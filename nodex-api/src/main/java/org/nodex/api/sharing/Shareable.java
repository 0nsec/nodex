package org.nodex.api.sharing;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Interface for objects that can be shared
 */
@NotNullByDefault
public interface Shareable {
    String getName();
}
