package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A private key for cryptographic operations.
 */
@NotNullByDefault
public interface PrivateKey {
    
    /**
     * Returns the key type.
     */
    String getKeyType();
    
    /**
     * Returns the encoded key bytes.
     */
    byte[] getEncoded();
    
    /**
     * Returns the key size in bytes.
     */
    int size();
}
