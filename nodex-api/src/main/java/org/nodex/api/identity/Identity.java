package org.nodex.api.identity;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public interface Identity {
    
    /**
     * Get the identity name.
     */
    String getName();
    
    /**
     * Get the public key.
     */
    byte[] getPublicKey();
    
    /**
     * Get the private key.
     */
    byte[] getPrivateKey();
}
