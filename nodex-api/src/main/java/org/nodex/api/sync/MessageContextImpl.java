package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.Collections;

@NotNullByDefault
public class MessageContextImpl implements MessageContext {
    
    private final Message message;
    private final Group group;
    private final Metadata metadata;
    private final Collection<GroupId> dependencies;
    
    public MessageContextImpl(Message message, Group group, Metadata metadata, Collection<GroupId> dependencies) {
        this.message = message;
        this.group = group;
        this.metadata = metadata;
        this.dependencies = dependencies;
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
    public Metadata getMetadata() {
        return metadata;
    }
    
    @Override
    public Collection<GroupId> getDependencies() {
        return dependencies;
    }
}
