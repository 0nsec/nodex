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
    
    /**
     * Creates a BDF dictionary with one entry.
     */
    public static BdfDictionary of(BdfEntry entry) {
        BdfDictionary dict = new BdfDictionary();
        dict.put(entry.getKey(), entry.getValue());
        return dict;
    }
    
    /**
     * Creates a BDF dictionary with two entries.
     */
    public static BdfDictionary of(BdfEntry entry1, BdfEntry entry2) {
        BdfDictionary dict = new BdfDictionary();
        dict.put(entry1.getKey(), entry1.getValue());
        dict.put(entry2.getKey(), entry2.getValue());
        return dict;
    }
    
    /**
     * Creates a BDF dictionary with three entries.
     */
    public static BdfDictionary of(BdfEntry entry1, BdfEntry entry2, BdfEntry entry3) {
        BdfDictionary dict = new BdfDictionary();
        dict.put(entry1.getKey(), entry1.getValue());
        dict.put(entry2.getKey(), entry2.getValue());
        dict.put(entry3.getKey(), entry3.getValue());
        return dict;
    }

    public String getString(String key) {
        Object obj = get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException("Value for key '" + key + "' is not a string");
    }
    
    public String getOptionalString(String key) {
        Object obj = get(key);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
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

    public int getInt(String key) {
        Object value = get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        throw new IllegalArgumentException("Expected integer for key: " + key);
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        Object value = get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return defaultValue;
    }

    public byte[] getRaw(String key) {
        return getBytes(key);
    }
    
    public byte[] getOptionalRaw(String key) {
        Object obj = get(key);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return null;
    }
}
