package org.nodex.api.cleanup;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Hook interface for cleanup operations.
 */
@NotNullByDefault
public interface CleanupHook {
    
    /**
     * Called when cleanup is needed.
     */
    void cleanup();
    
    /**
     * Returns the priority of this cleanup hook.
     * Higher numbers indicate higher priority.
     */
    int getPriority();
}
