package org.nodex.api.identity;

import org.nodex.api.Nameable;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * A pseudonym for a user.
 */
@Immutable
@NotNullByDefault
public class Author implements Nameable {

    /**
     * The current version of the author structure.
     */
    public static final int FORMAT_VERSION = 1;

    private final AuthorId id;
    private final int formatVersion;
    private final String name;
    private final byte[] publicKey;

    public Author(AuthorId id, int formatVersion, String name, byte[] publicKey) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be null or empty");
        }
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("Public key cannot be null or empty");
        }
        this.id = id;
        this.formatVersion = formatVersion;
        this.name = name.trim();
        this.publicKey = publicKey.clone();
    }

    /**
     * Returns the author's unique identifier.
     */
    public AuthorId getId() {
        return id;
    }

    /**
     * Returns the version of the author structure used to create the author.
     */
    public int getFormatVersion() {
        return formatVersion;
    }

    /**
     * Returns the author's name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the public key used to verify the pseudonym's signatures.
     */
    public byte[] getPublicKey() {
        return publicKey.clone();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Author && id.equals(((Author) o).id);
    }

    @Override
    public String toString() {
        return "Author{id=" + id + ", name='" + name + "'}";
    }
}
