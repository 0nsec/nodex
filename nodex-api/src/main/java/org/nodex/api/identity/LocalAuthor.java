package org.nodex.api.identity;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents a local author (author controlled by this instance)
 */
@NotNullByDefault
public class LocalAuthor extends Author {
    private final byte[] privateKey;

    public LocalAuthor(String name, byte[] publicKey, byte[] privateKey) {
        super(name, publicKey);
        if (privateKey == null || privateKey.length == 0) {
            throw new IllegalArgumentException("Private key cannot be null or empty");
        }
        this.privateKey = privateKey.clone();
    }

    public byte[] getPrivateKey() {
        return privateKey.clone();
    }

    @Override
    public String toString() {
        return "LocalAuthor{name='" + getName() + "'}";
    }
}
