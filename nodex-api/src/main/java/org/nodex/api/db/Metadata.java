package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.util.Map;

/**
 * Represents metadata associated with a database object.
 */
@NotNullByDefault
public interface Metadata {
    
    /**
     * Returns all key-value pairs in this metadata.
     */
    Map<String, Object> getAll();
    
    /**
     * Returns the value for the given key.
     */
    Object get(String key);
    
    /**
     * Returns the value for the given key as a string.
     */
    String getString(String key);
    
    /**
     * Returns the value for the given key as a long.
     */
    long getLong(String key);
    
    /**
     * Returns the value for the given key as a boolean.
     */
    boolean getBoolean(String key);
    
    /**
     * Returns the value for the given key as a byte array.
     */
    byte[] getBytes(String key);
    
    /**
     * Returns true if this metadata contains the given key.
     */
    boolean containsKey(String key);
    
    /**
     * Returns the number of key-value pairs in this metadata.
     */
    int size();
    
    /**
     * Returns true if this metadata is empty.
     */
    boolean isEmpty();
}
