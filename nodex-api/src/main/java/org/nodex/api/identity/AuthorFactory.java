package org.nodex.api.identity;

import org.nodex.api.crypto.KeyPair;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Factory for creating Author objects.
 */
@NotNullByDefault
public interface AuthorFactory {
    
    /**
     * Creates a new author with the given name and key pair.
     */
    LocalAuthor createLocalAuthor(String name, KeyPair keyPair);
    
    /**
     * Creates an author from the given parameters.
     */
    Author createAuthor(AuthorId id, String name, byte[] publicKey);
    
    /**
     * Creates an author ID from the given public key.
     */
    AuthorId createAuthorId(byte[] publicKey);
}
