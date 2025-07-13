package org.nodex.api.client;

import org.nodex.api.contact.Contact;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;

@NotNullByDefault
public interface ContactGroupFactoryExtensions {
    
    Group createLocalGroup(String clientId, int majorVersion);
    
    Group createContactGroup(String clientId, int majorVersion, Contact contact);
}
