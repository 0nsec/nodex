package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class PluginConfig {
    
    private final String pluginName;
    private final boolean enabled;
    
    public PluginConfig(String pluginName, boolean enabled) {
        this.pluginName = pluginName;
        this.enabled = enabled;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
