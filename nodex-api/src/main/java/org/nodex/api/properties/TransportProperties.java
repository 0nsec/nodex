package org.nodex.api.properties;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Map;

/**
 * Properties for transport plugins.
 */
@NotNullByDefault
public interface TransportProperties {
    
    /**
     * Returns the transport properties as a map.
     */
    Map<String, String> getProperties();
    
    /**
     * Returns the value of a specific property.
     */
    String getProperty(String key);
    
    /**
     * Sets a property value.
     */
    void setProperty(String key, String value);
    
    /**
     * Removes a property.
     */
    void removeProperty(String key);
}
