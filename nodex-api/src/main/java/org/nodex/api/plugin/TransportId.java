package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Represents a transport plugin identifier.
 */
@Immutable
@NotNullByDefault
public class TransportId {
    
    public static final TransportId BLUETOOTH = new TransportId("bluetooth");
    public static final TransportId LAN = new TransportId("lan");
    public static final TransportId TOR = new TransportId("tor");
    public static final TransportId MAILBOX = new TransportId("mailbox");
    
    private final String id;
    
    public TransportId(String id) {
        this.id = id;
    }
    
    public String getString() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransportId that = (TransportId) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return id;
    }
}
