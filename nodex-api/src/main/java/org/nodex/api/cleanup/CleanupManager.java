package org.nodex.api.cleanup;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Manager for cleaning up old data and messages.
 */
@NotNullByDefault
public interface CleanupManager {
    /**
     * Clean up old messages based on retention policy.
     */
    void cleanupOldMessages();
    
    /**
     * Clean up temporary files.
     */
    void cleanupTemporaryFiles();
    
    /**
     * Schedule cleanup task.
     */
    void scheduleCleanup();
}
