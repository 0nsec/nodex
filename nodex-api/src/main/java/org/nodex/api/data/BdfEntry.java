package org.nodex.api.data;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents an entry in a BDF dictionary.
 */
@NotNullByDefault
public interface BdfEntry {
    
    /**
     * Creates a new BdfEntry.
     */
    static BdfEntry of(String key, Object value) {
        return new BdfEntryImpl(key, value);
    }
    
    /**
     * Returns the key of this entry.
     */
    String getKey();
    
    /**
     * Returns the value of this entry.
     */
    Object getValue();
    
    /**
     * Returns true if the value is a string.
     */
    boolean isString();
    
    /**
     * Returns true if the value is a number.
     */
    boolean isNumber();
    
    /**
     * Returns true if the value is a boolean.
     */
    boolean isBoolean();
    
    /**
     * Returns true if the value is a byte array.
     */
    boolean isBytes();
    
    /**
     * Returns true if the value is a list.
     */
    boolean isList();
    
    /**
     * Returns true if the value is a dictionary.
     */
    boolean isDictionary();
    
    /**
     * Returns the value as a string.
     */
    String getString();
    
    /**
     * Returns the value as a long.
     */
    long getLong();
    
    /**
     * Returns the value as a boolean.
     */
    boolean getBoolean();
    
    /**
     * Returns the value as a byte array.
     */
    byte[] getBytes();
    
    /**
     * Returns the value as a BDF list.
     */
    BdfList getList();
    
    /**
     * Returns the value as a BDF dictionary.
     */
    BdfDictionary getDictionary();
}
