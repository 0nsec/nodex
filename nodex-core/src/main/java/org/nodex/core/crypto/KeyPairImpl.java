package org.nodex.core.crypto;

import org.nodex.api.crypto.KeyPair;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Implementation of KeyPair.
 */
@Immutable
@NotNullByDefault
public class KeyPairImpl implements KeyPair {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public KeyPairImpl(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public PublicKey getPublic() {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivate() {
        return privateKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyPairImpl)) return false;
        KeyPairImpl other = (KeyPairImpl) obj;
        return Objects.equals(publicKey, other.publicKey) &&
               Objects.equals(privateKey, other.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKey, privateKey);
    }

    @Override
    public String toString() {
        return "KeyPair{public=" + publicKey + ", private=" + privateKey + "}";
    }
}
