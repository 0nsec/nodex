package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for cryptographic operations - exact match to Briar.
 */
@NotNullByDefault
public class CryptoConstants {
    
    // Key lengths
    public static final int SECRET_KEY_BYTES = 32;
    public static final int PUBLIC_KEY_BYTES = 32;
    public static final int PRIVATE_KEY_BYTES = 32;
    
    // MAC and signature lengths
    public static final int MAC_BYTES = 32;
    public static final int MAX_SIGNATURE_BYTES = 64;
    public static final int SIGNATURE_BYTES = 64;
    
    // Nonce and salt lengths
    public static final int NONCE_BYTES = 24;
    public static final int SALT_BYTES = 32;
    
    // Hash lengths
    public static final int HASH_BYTES = 32;
    
    // Key types
    public static final String KEY_TYPE_AGREEMENT = "agreement";
    public static final String KEY_TYPE_SIGNATURE = "signature";
    
    private CryptoConstants() {
        // Utility class
    }
}
