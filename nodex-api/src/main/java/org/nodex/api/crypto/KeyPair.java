package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents a cryptographic key pair.
 */
@NotNullByDefault
public interface KeyPair {
    
    /**
     * Returns the private key.
     */
    PrivateKey getPrivate();
    
    /**
     * Returns the public key.
     */
    PublicKey getPublic();
}
