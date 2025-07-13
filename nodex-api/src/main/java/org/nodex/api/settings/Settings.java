package org.nodex.api.settings;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.Map;

@NotNullByDefault
public interface Settings {
    
    /**
     * Get a setting value.
     */
    String get(String key);
    
    /**
     * Set a setting value.
     */
    void put(String key, String value);
    
    /**
     * Get all settings as a map.
     */
    Map<String, String> getAll();
    
    /**
     * Remove a setting.
     */
    void remove(String key);
}
