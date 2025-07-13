package org.nodex.api.data;

import org.nodex.api.FormatException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public class BdfDictionaryExtensions {
    
    @Nullable
    public static Integer getOptionalInt(BdfDictionary dict, String key) {
        Object value = dict.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }
    
    public static boolean getBoolean(BdfDictionary dict, String key) throws FormatException {
        Object value = dict.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new FormatException("Expected boolean for key: " + key);
    }
    
    public static boolean getBoolean(BdfDictionary dict, String key, boolean defaultValue) {
        Object value = dict.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
}
