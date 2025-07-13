package org.nodex.api.transport;

import org.nodex.api.crypto.PublicKey;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Transport-specific cryptographic keys.
 */
@Immutable
@NotNullByDefault
public class TransportKeys {
    
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    
    public TransportKeys(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportKeys)) return false;
        TransportKeys that = (TransportKeys) o;
        return publicKey.equals(that.publicKey) && 
               privateKey.equals(that.privateKey);
    }
    
    @Override
    public int hashCode() {
        return publicKey.hashCode() ^ privateKey.hashCode();
    }
}
