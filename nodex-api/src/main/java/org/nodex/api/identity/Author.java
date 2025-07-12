package org.nodex.api.identity;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Represents an author in the system
 */
@NotNullByDefault
public class Author {
    private final String name;
    private final byte[] publicKey;

    public Author(String name, byte[] publicKey) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (publicKey == null || publicKey.length == 0) {
            throw new IllegalArgumentException("Public key cannot be null or empty");
        }
        this.name = name.trim();
        this.publicKey = publicKey.clone();
    }

    public String getName() {
        return name;
    }

    public byte[] getPublicKey() {
        return publicKey.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return name.equals(author.name) && java.util.Arrays.equals(publicKey, author.publicKey);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + java.util.Arrays.hashCode(publicKey);
        return result;
    }

    @Override
    public String toString() {
        return "Author{name='" + name + "'}";
    }
}
