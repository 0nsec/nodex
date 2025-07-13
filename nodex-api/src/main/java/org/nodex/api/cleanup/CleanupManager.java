package org.nodex.api.cleanup;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface CleanupManager {
    
    void registerCleanupHook(String clientId, int majorVersion, CleanupHook hook);
    
    void unregisterCleanupHook(String clientId, int majorVersion);
    
    void performCleanup();
}
