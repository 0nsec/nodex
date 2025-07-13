package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PasswordStrengthEstimator {
    
    /**
     * Estimate password strength (0.0 = weakest, 1.0 = strongest).
     */
    float estimateStrength(String password);
    
    /**
     * Check if password meets minimum strength requirements.
     */
    boolean isStrong(String password);
}
