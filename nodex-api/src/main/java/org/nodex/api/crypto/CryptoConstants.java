package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class CryptoConstants {
    
    public static final String KEY_TYPE_AGREEMENT = "ECDH";
    public static final String KEY_TYPE_SIGNATURE = "ECDSA";
    
    public static final int AES_KEY_BYTES = 32;
    public static final int MAC_BYTES = 16;
    public static final int SIGNATURE_BYTES = 64;
    
    private CryptoConstants() {} // Utility class
}
