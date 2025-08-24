package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class AgreementPublicKeyImpl implements AgreementPublicKey {
    private final byte[] encoded;
    private final String algorithm;
    private final String format;
    public AgreementPublicKeyImpl(byte[] encoded, String algorithm) {
        this.encoded = encoded.clone();
        this.algorithm = algorithm;
        this.format = "RAW";
    }
    @Override public byte[] getEncoded() { return encoded.clone(); }
    @Override public String getAlgorithm() { return algorithm; }
    @Override public String getFormat() { return format; }
}
