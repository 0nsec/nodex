package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Transport-specific properties.
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
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
    
    @Override
    public String toString() {
        return "TransportProperties{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
