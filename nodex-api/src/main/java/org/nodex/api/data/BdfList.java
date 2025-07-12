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
}
