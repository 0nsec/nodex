package org.nodex.core.crypto;

import org.nodex.api.crypto.PublicKey;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Implementation of PublicKey.
 */
@Immutable
@NotNullByDefault
public class PublicKeyImpl implements PublicKey {

    private final java.security.PublicKey javaPublicKey;
    private final byte[] encoded;

    public PublicKeyImpl(java.security.PublicKey javaPublicKey) {
        this.javaPublicKey = javaPublicKey;
        this.encoded = javaPublicKey.getEncoded();
    }

    public PublicKeyImpl(byte[] encoded) {
        this.encoded = encoded.clone();
        // TODO: Reconstruct Java PublicKey from encoded bytes
        this.javaPublicKey = null; // Placeholder
    }

    @Override
    public byte[] getEncoded() {
        return encoded.clone();
    }

    @Override
    public String getAlgorithm() {
        return javaPublicKey != null ? javaPublicKey.getAlgorithm() : "RSA";
    }

    @Override
    public String getFormat() {
        return javaPublicKey != null ? javaPublicKey.getFormat() : "X.509";
    }

    public java.security.PublicKey getJavaPublicKey() {
        return javaPublicKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PublicKeyImpl)) return false;
        PublicKeyImpl other = (PublicKeyImpl) obj;
        return Arrays.equals(encoded, other.encoded);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encoded);
    }

    @Override
    public String toString() {
        return "PublicKey{algorithm=" + getAlgorithm() + ", format=" + getFormat() + "}";
    }
}
