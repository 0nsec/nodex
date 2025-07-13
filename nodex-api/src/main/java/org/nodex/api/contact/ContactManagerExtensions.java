package org.nodex.api.contact;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ContactManagerExtensions {
    
    void registerContactHook(ContactHook hook);
    
    void unregisterContactHook(ContactHook hook);
}
