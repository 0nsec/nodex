package org.nodex.api.identity;

import org.nodex.api.crypto.KeyPair;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for identity operations.
 */
@NotNullByDefault
public interface IdentityManager {
    
    /**
     * Creates a new local identity.
     */
    LocalAuthor createLocalAuthor(String name);
    
    /**
     * Creates a new local identity with the given key pair.
     */
    LocalAuthor createLocalAuthor(String name, KeyPair keyPair);
    
    /**
     * Returns the local author if one exists.
     */
    LocalAuthor getLocalAuthor();
    
    /**
     * Returns true if a local author exists.
     */
    boolean hasLocalAuthor();
    
    /**
     * Stores the local author.
     */
    void storeLocalAuthor(LocalAuthor localAuthor);
}
