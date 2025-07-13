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
     * Registers a message validator with version.
     */
    void registerMessageValidator(String clientId, int majorVersion, MessageValidator validator);
    
    /**
     * Registers an incoming message hook with version.
     */
    void registerIncomingMessageHook(String clientId, int majorVersion, IncomingMessageHook hook);
    
    /**
     * Validates a message using the registered validators.
     */
    boolean validateMessage(String clientId, org.nodex.api.sync.Message message);
    
    /**
     * Validates a message.
     */
    ValidationResult validateMessage(org.nodex.api.sync.MessageContext context, org.nodex.api.data.BdfList body) throws org.nodex.api.FormatException;
}
