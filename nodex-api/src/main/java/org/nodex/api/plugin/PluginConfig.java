package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PluginConfig {
    String getPluginId();
    String getConfigValue(String key);
    void setConfigValue(String key, String value);
}
