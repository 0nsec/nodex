package org.nodex.core.api.crypto;

import org.nodex.api.crypto.CryptoConstants;

/**
 * Constants for crypto operations - exact match to Briar.
 */
public class CryptoConstants {
    
    /**
     * Re-export of CryptoConstants from the API.
     */
    public static final int MAC_BYTES = org.nodex.api.crypto.CryptoConstants.MAC_BYTES;
    public static final int MAX_SIGNATURE_BYTES = org.nodex.api.crypto.CryptoConstants.MAX_SIGNATURE_BYTES;
    
    private CryptoConstants() {
        // Utility class
    }
}
