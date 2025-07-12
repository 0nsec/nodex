package org.nodex.core.plugin;

import org.nodex.api.plugin.PluginManager;
import org.nodex.api.plugin.Plugin;
import org.nodex.api.plugin.TransportPlugin;
import org.nodex.api.plugin.PluginConfig;
import org.nodex.api.transport.TransportId;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Implementation of PluginManager that manages transport and other plugins.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class PluginManagerImpl implements PluginManager, Service {

    private static final Logger LOG = Logger.getLogger(PluginManagerImpl.class.getName());

    private final ConcurrentHashMap<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TransportId, TransportPlugin> transportPlugins = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<PluginConfig> pluginConfigs = new CopyOnWriteArrayList<>();
    
    private volatile boolean started = false;

    @Inject
    public PluginManagerImpl() {
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        LOG.info("Starting PluginManager");
        
        try {
            // Initialize built-in plugins
            initializeBuiltinPlugins();
            
            // Start all plugins
            for (Plugin plugin : plugins.values()) {
                if (plugin instanceof Service) {
                    ((Service) plugin).startService();
                }
            }
            
            started = true;
            LOG.info("PluginManager started successfully");
        } catch (Exception e) {
            throw new ServiceException("Failed to start PluginManager", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping PluginManager");
        
        try {
            // Stop all plugins
            for (Plugin plugin : plugins.values()) {
                if (plugin instanceof Service) {
                    try {
                        ((Service) plugin).stopService();
                    } catch (Exception e) {
                        LOG.warning("Error stopping plugin: " + e.getMessage());
                    }
                }
            }
            
            plugins.clear();
            transportPlugins.clear();
            started = false;
            
            LOG.info("PluginManager stopped successfully");
        } catch (Exception e) {
            throw new ServiceException("Failed to stop PluginManager", e);
        }
    }

    @Override
    public void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getClass().getName(), plugin);
        
        if (plugin instanceof TransportPlugin) {
            TransportPlugin transportPlugin = (TransportPlugin) plugin;
            transportPlugins.put(transportPlugin.getId(), transportPlugin);
        }
        
        LOG.info("Registered plugin: " + plugin.getClass().getSimpleName());
    }

    @Override
    public void unregisterPlugin(Plugin plugin) {
        plugins.remove(plugin.getClass().getName());
        
        if (plugin instanceof TransportPlugin) {
            TransportPlugin transportPlugin = (TransportPlugin) plugin;
            transportPlugins.remove(transportPlugin.getId());
        }
        
        LOG.info("Unregistered plugin: " + plugin.getClass().getSimpleName());
    }

    @Override
    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Plugin> T getPlugin(Class<T> pluginClass) {
        Plugin plugin = plugins.get(pluginClass.getName());
        if (plugin != null && pluginClass.isInstance(plugin)) {
            return (T) plugin;
        }
        return null;
    }

    @Override
    public Collection<TransportPlugin> getTransportPlugins() {
        return transportPlugins.values();
    }

    @Override
    public TransportPlugin getTransportPlugin(TransportId transportId) {
        return transportPlugins.get(transportId);
    }

    @Override
    public void addPluginConfig(PluginConfig config) {
        pluginConfigs.add(config);
        LOG.info("Added plugin config: " + config.getClass().getSimpleName());
    }

    @Override
    public Collection<PluginConfig> getPluginConfigs() {
        return pluginConfigs;
    }

    private void initializeBuiltinPlugins() {
        LOG.info("Initializing built-in plugins");
        
        // In a real implementation, this would initialize transport plugins like:
        // - BluetoothPlugin
        // - LanPlugin  
        // - TorPlugin
        // - MailboxPlugin
        
        // For now, we'll create placeholder implementations
        LOG.info("Built-in plugins initialized");
    }
}
