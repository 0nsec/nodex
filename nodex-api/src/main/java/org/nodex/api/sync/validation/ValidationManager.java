package org.nodex.api.sync.validation;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for message validation operations.
 */
@NotNullByDefault
public interface ValidationManager {
    
    /**
     * Registers a message validator for a specific client.
     */
    void registerMessageValidator(String clientId, MessageValidator validator);
    
    /**
     * Unregisters a message validator for a specific client.
     */
    void unregisterMessageValidator(String clientId);
    
    /**
     * Registers an incoming message hook.
     */
    void registerIncomingMessageHook(String clientId, IncomingMessageHook hook);
    
    /**
     * Unregisters an incoming message hook.
     */
    void unregisterIncomingMessageHook(String clientId);
    
    /**
     * Validates a message using the registered validators.
     */
    boolean validateMessage(String clientId, org.nodex.api.sync.Message message);
}
