package org.nodex.api.system;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * System clock interface for time operations.
 */
@NotNullByDefault
public interface Clock {
    /**
     * Get current time in milliseconds.
     */
    long currentTimeMillis();
}
