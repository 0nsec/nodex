package org.nodex.core.crypto;

import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Implementation of PrivateKey.
 */
@Immutable
@NotNullByDefault
public class PrivateKeyImpl implements PrivateKey {

    private final java.security.PrivateKey javaPrivateKey;
    private final byte[] encoded;

    public PrivateKeyImpl(java.security.PrivateKey javaPrivateKey) {
        this.javaPrivateKey = javaPrivateKey;
        this.encoded = javaPrivateKey.getEncoded();
    }

    public PrivateKeyImpl(byte[] encoded) {
        this.encoded = encoded.clone();
        // TODO: Reconstruct Java PrivateKey from encoded bytes
        this.javaPrivateKey = null; // Placeholder
    }

    @Override
    public byte[] getEncoded() {
        return encoded.clone();
    }

    @Override
    public String getAlgorithm() {
        return javaPrivateKey != null ? javaPrivateKey.getAlgorithm() : "RSA";
    }

    @Override
    public String getFormat() {
        return javaPrivateKey != null ? javaPrivateKey.getFormat() : "PKCS#8";
    }

    public java.security.PrivateKey getJavaPrivateKey() {
        return javaPrivateKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PrivateKeyImpl)) return false;
        PrivateKeyImpl other = (PrivateKeyImpl) obj;
        return Arrays.equals(encoded, other.encoded);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encoded);
    }

    @Override
    public String toString() {
        return "PrivateKey{algorithm=" + getAlgorithm() + ", format=" + getFormat() + "}";
    }
}
