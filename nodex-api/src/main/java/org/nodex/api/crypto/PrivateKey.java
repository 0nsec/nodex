package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents a private key.
 */
@NotNullByDefault
public interface PrivateKey {
    
    /**
     * Returns the encoded form of this private key.
     */
    byte[] getEncoded();
    
    /**
     * Returns the algorithm name for this private key.
     */
    String getAlgorithm();
    
    /**
     * Returns the format of this private key.
     */
    String getFormat();
}
