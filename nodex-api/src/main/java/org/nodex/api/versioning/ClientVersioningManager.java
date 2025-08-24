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
     * Register a client.
     */
    void registerClient(String clientId, int majorVersion, int minorVersion, Object client);
    
    /**
     * Get client visibility.
     */
    org.nodex.api.sync.Visibility getClientVisibility(org.nodex.api.db.Transaction txn, org.nodex.api.contact.ContactId contactId, String clientId, int majorVersion) throws org.nodex.api.db.DbException;
    default int getClientMinorVersion(org.nodex.api.db.Transaction txn, org.nodex.api.contact.ContactId contactId, org.nodex.api.sync.ClientId clientId, int majorVersion, int defaultValue) { return defaultValue; }
    
    /**
     * Unregister a client.
     */
    void unregisterClient(String clientId);
    
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
