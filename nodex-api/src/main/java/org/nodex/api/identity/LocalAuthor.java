package org.nodex.api.identity;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * A pseudonym for the local user.
 */
@Immutable
@NotNullByDefault
public class LocalAuthor extends Author {

    private final byte[] privateKey;

    public LocalAuthor(AuthorId id, int formatVersion, String name,
                      byte[] publicKey, byte[] privateKey) {
        super(id, formatVersion, name, publicKey);
        if (privateKey == null || privateKey.length == 0) {
            throw new IllegalArgumentException("Private key cannot be null or empty");
        }
        this.privateKey = privateKey.clone();
    }

    /**
     * Returns the private key used to generate the pseudonym's signatures.
     */
    public byte[] getPrivateKey() {
        return privateKey.clone();
    }
}

    public byte[] getPrivateKey() {
        return privateKey.clone();
    }

    @Override
    public String toString() {
        return "LocalAuthor{name='" + getName() + "'}";
    }
}
