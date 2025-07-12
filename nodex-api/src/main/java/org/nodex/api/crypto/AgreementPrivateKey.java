package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Agreement private key for key agreement protocols.
 */
@Immutable
@NotNullByDefault
public interface AgreementPrivateKey {

    /**
     * Returns the encoded form of the private key.
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
