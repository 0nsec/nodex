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
}
