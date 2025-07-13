package org.nodex.api.sync;

import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class BdfMessageContextImpl implements BdfMessageContext {
    
    private final Message message;
    private final BdfList bdfList;
    private final BdfDictionary dictionary;
    private final long timestamp;
    
    public BdfMessageContextImpl(Message message, BdfList bdfList, BdfDictionary dictionary, long timestamp) {
        this.message = message;
        this.bdfList = bdfList;
        this.dictionary = dictionary;
        this.timestamp = timestamp;
    }
    
    @Override
    public Message getMessage() {
        return message;
    }
    
    @Override
    public BdfList getBdfList() {
        return bdfList;
    }
    
    @Override
    public BdfDictionary getDictionary() {
        return dictionary;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
