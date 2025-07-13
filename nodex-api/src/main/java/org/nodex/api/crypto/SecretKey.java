package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents a secret key for symmetric encryption.
 */
@NotNullByDefault
public interface SecretKey {
    
    /**
     * Returns the encoded form of this secret key.
     */
    byte[] getEncoded();
    
    /**
     * Returns the algorithm name for this secret key.
     */
    String getAlgorithm();
    
    /**
     * Returns the format of this secret key.
     */
    String getFormat();
}
