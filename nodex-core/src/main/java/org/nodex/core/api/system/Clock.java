package org.nodex.core.api.system;

import org.nodex.api.system.Clock;

/**
 * Constants for system clock - exact match to Briar.
 */
public class ClockConstants {
    
    /**
     * Minimum reasonable time in milliseconds since Unix epoch.
     */
    public static final long MIN_REASONABLE_TIME_MS = 1000L * 60 * 60 * 24 * 365 * 10; // 10 years
    
    private ClockConstants() {
        // Utility class
    }
}
