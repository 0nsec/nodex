package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.Collections;

@NotNullByDefault
public class MessageContextImpl extends MessageContext {
    
    private final Metadata metadata;
    private final Collection<GroupId> dependencies;
    
    public MessageContextImpl(Metadata metadata, Collection<GroupId> dependencies) {
        this.metadata = metadata;
        this.dependencies = dependencies;
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
