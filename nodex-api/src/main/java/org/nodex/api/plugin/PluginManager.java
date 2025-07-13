package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.transport.TransportId;
import org.nodex.api.lifecycle.Service;
import java.util.Collection;

@NotNullByDefault
public interface PluginManager extends Service {
    
    void registerPlugin(Plugin plugin);
    void unregisterPlugin(Plugin plugin);
    Collection<Plugin> getPlugins();
    <T extends Plugin> T getPlugin(Class<T> pluginClass);
    Collection<TransportPlugin> getTransportPlugins();
    TransportPlugin getTransportPlugin(TransportId transportId);
    void addPluginConfig(PluginConfig config);
    Collection<PluginConfig> getPluginConfigs();
}
