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
    default void registerIncomingMessageHook(org.nodex.api.sync.ClientId clientId, IncomingMessageHook hook) {
        registerIncomingMessageHook(clientId.toString(), hook);
    }
    
    /**
     * Unregisters an incoming message hook.
     */
    void unregisterIncomingMessageHook(String clientId);
    default void unregisterIncomingMessageHook(org.nodex.api.sync.ClientId clientId) {
        unregisterIncomingMessageHook(clientId.toString());
    }
    
    /**
     * Registers a message validator with version.
     */
    void registerMessageValidator(String clientId, int majorVersion, MessageValidator validator);
    default void registerMessageValidator(org.nodex.api.sync.ClientId clientId, int majorVersion, MessageValidator validator) {
        registerMessageValidator(clientId.toString(), majorVersion, validator);
    }
    
    /**
     * Registers an incoming message hook with version.
     */
    void registerIncomingMessageHook(String clientId, int majorVersion, IncomingMessageHook hook);
    default void registerIncomingMessageHook(org.nodex.api.sync.ClientId clientId, int majorVersion, IncomingMessageHook hook) {
        registerIncomingMessageHook(clientId.toString(), majorVersion, hook);
    }
    
    /**
     * Validates a message using the registered validators.
     */
    boolean validateMessage(String clientId, org.nodex.api.sync.Message message);
    
    /**
     * Validates a message.
     */
    ValidationResult validateMessage(org.nodex.api.sync.MessageContext context, org.nodex.api.data.BdfList body) throws org.nodex.api.FormatException;
}
