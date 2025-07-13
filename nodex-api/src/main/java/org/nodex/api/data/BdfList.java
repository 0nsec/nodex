package org.nodex.api.data;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Data Format List
 */
@NotNullByDefault
public class BdfList extends ArrayList<Object> {
    
    public BdfList() {
        super();
    }

    public BdfList(List<Object> list) {
        super(list);
    }

    public String getString(int index) {
        Object obj = get(index);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new IllegalArgumentException("Item at index " + index + " is not a string");
    }

    public Long getLong(int index) {
        Object obj = get(index);
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        throw new IllegalArgumentException("Item at index " + index + " is not a number");
    }

    public BdfList getList(int index) {
        Object obj = get(index);
        if (obj instanceof BdfList) {
            return (BdfList) obj;
        }
        throw new IllegalArgumentException("Item at index " + index + " is not a list");
    }

    public byte[] getBytes(int index) {
        Object obj = get(index);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        throw new IllegalArgumentException("Item at index " + index + " is not a byte array");
    }
    public static BdfList of(Object... objects) {
        BdfList list = new BdfList();
        for (Object obj : objects) {
            list.add(obj);
        }
        return list;
    }

    public String getOptionalString(int index) {
        if (index >= size()) {
            return null;
        }
        Object obj = get(index);
        return obj instanceof String ? (String) obj : null;
    }

    public Long getOptionalLong(int index) {
        if (index >= size()) {
            return null;
        }
        Object obj = get(index);
        return obj instanceof Long ? (Long) obj : null;
    }

    public int getInt(int index) {
        Object obj = get(index);
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        throw new IllegalArgumentException("Expected integer at index " + index);
    }

    public byte[] getRaw(int index) {
        Object obj = get(index);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        throw new IllegalArgumentException("Expected byte array at index " + index);
    }

    public boolean getBoolean(int index) {
        Object obj = get(index);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        throw new IllegalArgumentException("Expected boolean at index " + index);
    }
}
