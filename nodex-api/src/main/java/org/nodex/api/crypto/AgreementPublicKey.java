package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Agreement public key for key agreement protocols.
 */
@Immutable
@NotNullByDefault
public interface AgreementPublicKey {

    /**
     * Returns the encoded form of the public key.
     */
    byte[] getEncoded();

    /**
     * Returns the algorithm name.
     */
    String getAlgorithm();

    /**
     * Returns the format of the encoded key.
     */
    String getFormat();
}
