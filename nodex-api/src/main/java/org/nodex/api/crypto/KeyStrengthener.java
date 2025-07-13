package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface KeyStrengthener {
    
    /**
     * Strengthen a key using additional entropy.
     */
    byte[] strengthenKey(byte[] key, byte[] salt, int iterations);
    
    /**
     * Get the recommended number of iterations.
     */
    int getRecommendedIterations();
}
