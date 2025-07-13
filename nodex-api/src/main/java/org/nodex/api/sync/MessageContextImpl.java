package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.Collections;

@NotNullByDefault
public class MessageContextImpl implements MessageContext {
    
    private final Message message;
    private final Group group;
    private final long timestamp;
    private final boolean shouldDeliver;
    private final boolean shouldShare;
    
    public MessageContextImpl(Message message, Group group, long timestamp, boolean shouldDeliver, boolean shouldShare) {
        this.message = message;
        this.group = group;
        this.timestamp = timestamp;
        this.shouldDeliver = shouldDeliver;
        this.shouldShare = shouldShare;
    }
    
    @Override
    public Message getMessage() {
        return message;
    }
    
    @Override
    public Group getGroup() {
        return group;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean shouldDeliver() {
        return shouldDeliver;
    }
    
    @Override
    public boolean shouldShare() {
        return shouldShare;
    }
}
