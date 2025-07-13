package org.nodex.api.sync.validation;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ValidationManagerExtensions {
    
    void registerMessageValidator(String clientId, int majorVersion, MessageValidator validator);
    
    void registerIncomingMessageHook(String clientId, int majorVersion, IncomingMessageHook hook);
}
