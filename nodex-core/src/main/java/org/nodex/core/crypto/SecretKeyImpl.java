package org.nodex.core.crypto;

import org.nodex.api.crypto.SecretKey;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Implementation of SecretKey.
 */
@Immutable
@NotNullByDefault
public class SecretKeyImpl implements SecretKey {

    private final byte[] encoded;

    public SecretKeyImpl(byte[] encoded) {
        this.encoded = encoded.clone();
    }

    @Override
    public byte[] getEncoded() {
        return encoded.clone();
    }

    @Override
    public String getAlgorithm() {
        return "AES";
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SecretKeyImpl)) return false;
        SecretKeyImpl other = (SecretKeyImpl) obj;
        return Arrays.equals(encoded, other.encoded);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encoded);
    }

    @Override
    public String toString() {
        return "SecretKey{algorithm=" + getAlgorithm() + ", format=" + getFormat() + "}";
    }
}
