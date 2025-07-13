package org.nodex.api.data;

import org.nodex.api.FormatException;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;

import javax.annotation.Nullable;

@NotNullByDefault
public class BdfListExtensions {
    
    public static BdfList of(Object... objects) {
        BdfList list = new BdfList();
        for (Object obj : objects) {
            list.add(obj);
        }
        return list;
    }
    
    @Nullable
    public static String getOptionalString(BdfList list, int index) throws FormatException {
        if (index >= list.size()) {
            return null;
        }
        Object obj = list.get(index);
        return obj instanceof String ? (String) obj : null;
    }
    
    @Nullable
    public static Long getOptionalLong(BdfList list, int index) throws FormatException {
        if (index >= list.size()) {
            return null;
        }
        Object obj = list.get(index);
        return obj instanceof Long ? (Long) obj : null;
    }
    
    public static int getInt(BdfList list, int index) throws FormatException {
        Object obj = list.get(index);
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        throw new FormatException("Expected integer at index " + index);
    }
    
    public static byte[] getRaw(BdfList list, int index) throws FormatException {
        Object obj = list.get(index);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        throw new FormatException("Expected byte array at index " + index);
    }
}
