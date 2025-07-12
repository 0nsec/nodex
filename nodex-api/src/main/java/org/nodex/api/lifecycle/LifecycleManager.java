package org.nodex.api.lifecycle;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for handling application lifecycle events.
 */
@NotNullByDefault
public interface LifecycleManager {
    
    /**
     * Registers a hook to be called when the database is opened.
     */
    void registerOpenDatabaseHook(OpenDatabaseHook hook);
    
    /**
     * Registers a hook to be called when the database is closed.
     */
    void registerCloseDatabaseHook(CloseDatabaseHook hook);
    
    /**
     * Starts the lifecycle manager.
     */
    void start();
    
    /**
     * Stops the lifecycle manager.
     */
    void stop();
    
    /**
     * Returns true if the lifecycle manager is running.
     */
    boolean isRunning();
    
    /**
     * Hook interface for database open events.
     */
    interface OpenDatabaseHook {
        /**
         * Called when the database is opened.
         */
        void onDatabaseOpened();
    }
    
    /**
     * Hook interface for database close events.
     */
    interface CloseDatabaseHook {
        /**
         * Called when the database is closed.
         */
        void onDatabaseClosed();
    }
}
