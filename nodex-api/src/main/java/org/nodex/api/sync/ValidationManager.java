package org.nodex.api.sync;

import org.nodex.api.sync.validation.MessageValidator;
import org.nodex.api.sync.validation.IncomingMessageHook;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

/**
 * Manages message validation and delivery.
 */
@NotNullByDefault
public interface ValidationManager extends Service {
    
    /**
     * Register a message validator.
     */
    void registerMessageValidator(MessageValidator validator);
    
    /**
     * Register an incoming message hook.
     */
    void registerIncomingMessageHook(IncomingMessageHook hook);
    
    /**
     * Validate a message.
     */
    boolean validateMessage(Message message);
}
