package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Transport-specific properties for configuration.
 */
@Immutable
@NotNullByDefault
public class TransportProperties {
    
    private final String key;
    private final String value;
    
    public TransportProperties(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportProperties)) return false;
        TransportProperties that = (TransportProperties) o;
        return key.equals(that.key) && value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return key.hashCode() * 31 + value.hashCode();
    }
    
    @Override
    public String toString() {
        return key + "=" + value;
    }
}
