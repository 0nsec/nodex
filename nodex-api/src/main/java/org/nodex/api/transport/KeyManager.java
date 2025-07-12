package org.nodex.api.transport;

import org.nodex.api.crypto.SecretKey;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;

import java.util.Collection;

/**
 * Manages transport keys for secure communication.
 */
@NotNullByDefault
public interface KeyManager {

    /**
     * Generates a new transport key.
     */
    SecretKey generateTransportKey();

    /**
     * Stores a transport key.
     */
    void storeTransportKey(SecretKey key);

    /**
     * Retrieves a transport key.
     */
    SecretKey getTransportKey();

    /**
     * Removes a transport key.
     */
    void removeTransportKey(SecretKey key);

    /**
     * Returns all transport keys.
     */
    Collection<SecretKey> getAllTransportKeys();
}
