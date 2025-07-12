package org.nodex.api.properties;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties for a transport plugin.
 */
@NotNullByDefault
public class TransportProperties {
    
    private final Map<String, String> properties;
    
    public TransportProperties() {
        this.properties = new HashMap<>();
    }
    
    public TransportProperties(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }
    
    public void put(String key, String value) {
        properties.put(key, value);
    }
    
    public String get(String key) {
        return properties.get(key);
    }
    
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    public Map<String, String> getAll() {
        return new HashMap<>(properties);
    }
    
    public void putAll(Map<String, String> props) {
        properties.putAll(props);
    }
    
    public void remove(String key) {
        properties.remove(key);
    }
    
    public boolean isEmpty() {
        return properties.isEmpty();
    }
    
    @Override
    public String toString() {
        return properties.toString();
    }
}
