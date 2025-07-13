package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public interface TransportKeys {
    
    /**
     * Get the transport ID these keys belong to.
     */
    TransportId getTransportId();
    
    /**
     * Get the public key.
     */
    byte[] getPublicKey();
    
    /**
     * Get the private key.
     */
    byte[] getPrivateKey();
}
