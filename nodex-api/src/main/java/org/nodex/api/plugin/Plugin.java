package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Plugin {
    
    /**
     * Get the plugin ID.
     */
    String getId();
    
    /**
     * Get the plugin version.
     */
    String getVersion();
}
