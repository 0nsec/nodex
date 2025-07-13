package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public interface PublicKey {
    
    /**
     * Get the encoded form of this public key.
     */
    byte[] getEncoded();
    
    /**
     * Get the key type (e.g., "ECDH", "ECDSA").
     */
    String getKeyType();
}
