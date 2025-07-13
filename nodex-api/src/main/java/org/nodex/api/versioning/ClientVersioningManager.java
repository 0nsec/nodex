package org.nodex.api.versioning;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for handling client versioning.
 */
@NotNullByDefault
public interface ClientVersioningManager {
    
    /**
     * Registers a client versioning hook.
     */
    void registerClientVersioningHook(ClientVersioningHook hook);
    
    /**
     * Unregisters a client versioning hook.
     */
    void unregisterClientVersioningHook(ClientVersioningHook hook);
    
    /**
     * Gets the supported version for a client.
     */
    int getSupportedVersion(String clientId);
    
    /**
     * Sets the supported version for a client.
     */
    void setSupportedVersion(String clientId, int majorVersion, int minorVersion);
    
    /**
     * Hook interface for client versioning events.
     */
    interface ClientVersioningHook {
        /**
         * Called when a client version is updated.
         */
        void onClientVersionUpdated(String clientId, int majorVersion, int minorVersion);
    }
}
