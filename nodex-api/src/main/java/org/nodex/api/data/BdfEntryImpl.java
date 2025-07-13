package org.nodex.api.data;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class BdfEntryImpl implements BdfEntry {
    
    private final String key;
    private final Object value;
    
    public BdfEntryImpl(String key, Object value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean isString() {
        return value instanceof String;
    }
    
    @Override
    public boolean isNumber() {
        return value instanceof Number;
    }
    
    @Override
    public boolean isBoolean() {
        return value instanceof Boolean;
    }
    
    @Override
    public boolean isBytes() {
        return value instanceof byte[];
    }
    
    @Override
    public boolean isList() {
        return value instanceof BdfList;
    }
    
    @Override
    public boolean isDictionary() {
        return value instanceof BdfDictionary;
    }
    
    @Override
    public String getString() {
        if (value instanceof String) {
            return (String) value;
        }
        throw new ClassCastException("Value is not a string");
    }
    
    @Override
    public long getLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new ClassCastException("Value is not a number");
    }
    
    @Override
    public boolean getBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new ClassCastException("Value is not a boolean");
    }
    
    @Override
    public byte[] getBytes() {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        throw new ClassCastException("Value is not a byte array");
    }
    
    @Override
    public BdfList getList() {
        if (value instanceof BdfList) {
            return (BdfList) value;
        }
        throw new ClassCastException("Value is not a BdfList");
    }
    
    @Override
    public BdfDictionary getDictionary() {
        if (value instanceof BdfDictionary) {
            return (BdfDictionary) value;
        }
        throw new ClassCastException("Value is not a BdfDictionary");
    }
}
