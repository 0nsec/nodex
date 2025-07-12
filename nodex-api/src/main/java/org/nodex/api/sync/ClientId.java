package org.nodex.api.sync;

import org.nodex.api.UniqueId;

/**
 * Unique identifier for a client
 */
public class ClientId extends UniqueId {
    public ClientId(String clientId) {
        super(clientId.getBytes());
    }

    public ClientId(byte[] id) {
        super(id);
    }

    public String getString() {
        return new String(id);
    }
}
