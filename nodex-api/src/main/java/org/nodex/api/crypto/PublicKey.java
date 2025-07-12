package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents a public key.
 */
@NotNullByDefault
public interface PublicKey {
    
    /**
     * Returns the encoded form of this public key.
     */
    byte[] getEncoded();
    
    /**
     * Returns the algorithm name for this public key.
     */
    String getAlgorithm();
    
    /**
     * Returns the format of this public key.
     */
    String getFormat();
}
