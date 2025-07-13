package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collections;

@NotNullByDefault
public class BdfMessageContextImpl extends BdfMessageContext {
    
    private final Metadata metadata;
    
    public BdfMessageContextImpl(Metadata metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
    
    @Override
    public java.util.Collection<GroupId> getDependencies() {
        return Collections.emptyList();
    }
}
