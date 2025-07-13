package org.nodex.api.data;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Binary Data Format Dictionary
 */
@NotNullByDefault
public class BdfDictionary extends HashMap<String, Object> {
    
    /**
     * Null value constant for BDF dictionaries.
     */
    public static final Object NULL_VALUE = new Object();

    public BdfDictionary() {
        super();
    }

    public BdfDictionary(Map<String, Object> map) {
        super(map);
    }

    public String getString(String key) {
        Object obj = get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a string");
    }

    public Long getLong(String key) {
        Object obj = get(key);
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a number");
    }

    public BdfList getList(String key) {
        Object obj = get(key);
        if (obj instanceof BdfList) {
            return (BdfList) obj;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a list");
    }

    public BdfDictionary getDictionary(String key) {
        Object obj = get(key);
        if (obj instanceof BdfDictionary) {
            return (BdfDictionary) obj;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a dictionary");
    }

    public byte[] getBytes(String key) {
        Object obj = get(key);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a byte array");
    }
    public Integer getOptionalInt(String key) {
        Object value = get(key);
        return value instanceof Integer ? (Integer) value : null;
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IllegalArgumentException("Expected boolean for key: " + key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}
