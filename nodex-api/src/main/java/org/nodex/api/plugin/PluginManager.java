package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;

@NotNullByDefault
public interface PluginManager {
    
    void registerPlugin(Plugin plugin);
    
    void unregisterPlugin(Plugin plugin);
    
    Collection<Plugin> getPlugins();
    
    <T extends Plugin> T getPlugin(Class<T> pluginClass);
    
    boolean isPluginRegistered(Class<? extends Plugin> pluginClass);
    
    void addPluginConfig(PluginConfig config);
    
    Collection<PluginConfig> getPluginConfigs();
}
