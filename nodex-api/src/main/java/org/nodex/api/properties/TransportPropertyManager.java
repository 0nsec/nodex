package org.nodex.api.properties;

import org.nodex.api.plugin.TransportId;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for transport properties.
 */
@NotNullByDefault
public interface TransportPropertyManager {
    
    /**
     * Returns the properties for a specific transport.
     */
    TransportProperties getProperties(TransportId transportId);
    
    /**
     * Sets the properties for a specific transport.
     */
    void setProperties(TransportId transportId, TransportProperties properties);
    
    /**
     * Removes properties for a specific transport.
     */
    void removeProperties(TransportId transportId);
}
