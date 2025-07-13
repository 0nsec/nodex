package org.nodex.api.sync;

import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class BdfMessageContextImpl implements MessageContext, BdfMessageContext {
    private final Group group;
    private final boolean deliver;
    private final boolean share;
    
    private final Message message;
    private final BdfList bdfList;
    private final BdfDictionary dictionary;
    private final long timestamp;
    
    public BdfMessageContextImpl(Message message, BdfList bdfList, BdfDictionary dictionary, long timestamp) {
        this(message, bdfList, dictionary, timestamp, null, true, false);
    }

    public BdfMessageContextImpl(Message message, BdfList bdfList, BdfDictionary dictionary, long timestamp, Group group, boolean deliver, boolean share) {
        this.message = message;
        this.bdfList = bdfList;
        this.dictionary = dictionary;
        this.timestamp = timestamp;
        this.group = group;
        this.deliver = deliver;
        this.share = share;
    }
    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean shouldDeliver() {
        return deliver;
    }

    @Override
    public boolean shouldShare() {
        return share;
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
