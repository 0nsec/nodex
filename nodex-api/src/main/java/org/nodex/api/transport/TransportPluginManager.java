package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.plugin.TransportId;

import java.util.Collection;

@NotNullByDefault
public interface TransportPluginManager {
    
    void registerPlugin(TransportPlugin plugin);
    
    void unregisterPlugin(TransportPlugin plugin);
    
    Collection<TransportPlugin> getPlugins();
    
    TransportPlugin getPlugin(TransportId transportId);
    
    boolean isPluginRegistered(TransportId transportId);
}
