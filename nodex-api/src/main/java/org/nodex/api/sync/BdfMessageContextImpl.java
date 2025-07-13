package org.nodex.api.sync;

import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.data.BdfList;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collections;

@NotNullByDefault
public class BdfMessageContextImpl implements BdfMessageContext {
    
    private final Message message;
    private final BdfList bdfList;
    
    public BdfMessageContextImpl(Message message, BdfList bdfList) {
        this.message = message;
        this.bdfList = bdfList;
    }
    
    @Override
    public Message getMessage() {
        return message;
    }
    
    @Override
    public BdfList getBdfList() {
        return bdfList;
    }
}
