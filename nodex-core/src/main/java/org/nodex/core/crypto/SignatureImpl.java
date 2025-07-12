package org.nodex.core.crypto;

import org.nodex.api.crypto.Signature;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Implementation of Signature.
 */
@Immutable
@NotNullByDefault
public class SignatureImpl implements Signature {

    private final byte[] bytes;

    public SignatureImpl(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    @Override
    public byte[] getBytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SignatureImpl)) return false;
        SignatureImpl other = (SignatureImpl) obj;
        return Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "Signature{length=" + bytes.length + "}";
    }
}
