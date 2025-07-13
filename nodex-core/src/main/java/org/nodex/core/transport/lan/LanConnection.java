package org.nodex.core.transport.lan;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * Represents a LAN connection to a contact.
 */
@ThreadSafe
@NotNullByDefault
public class LanConnection {
    
    private final ContactId contactId;
    private volatile boolean connected = true;
    
    public LanConnection(ContactId contactId) {
        this.contactId = contactId;
    }
    
    public ContactId getContactId() {
        return contactId;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void close() throws IOException {
        connected = false;
    }
}
