package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.Map;

@NotNullByDefault
public interface TransportProperties {
    
    Map<String, String> getProperties();
    String getProperty(String key);
    void setProperty(String key, String value);
}
