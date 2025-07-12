package org.nodex.core.identity;

import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.crypto.KeyPair;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.identity.AuthorId;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Implementation of AuthorFactory that creates Author instances.
 */
@Immutable
@NotNullByDefault
public class AuthorFactoryImpl implements AuthorFactory {

    private static final Logger LOG = Logger.getLogger(AuthorFactoryImpl.class.getName());

    private final CryptoComponent crypto;
    private final SecureRandom random;

    @Inject
    public AuthorFactoryImpl(CryptoComponent crypto) {
        this.crypto = crypto;
        this.random = new SecureRandom();
    }

    @Override
    public LocalAuthor createLocalAuthor(String name) {
        // Generate a new key pair for the author
        KeyPair keyPair = crypto.generateKeyPair();
        
        // Create author ID from public key
        AuthorId authorId = new AuthorId(crypto.hash(keyPair.getPublic().getEncoded()));
        
        // Create and return local author
        return new LocalAuthor(authorId, name, keyPair.getPublic(), keyPair.getPrivate());
    }

    @Override
    public Author createAuthor(AuthorId authorId, String name, PublicKey publicKey) {
        return new Author(authorId, name, publicKey);
    }

    @Override
    public Author createAuthor(String name, PublicKey publicKey) {
        // Generate author ID from public key
        AuthorId authorId = new AuthorId(crypto.hash(publicKey.getEncoded()));
        return new Author(authorId, name, publicKey);
    }

    @Override
    public AuthorId createAuthorId() {
        // Generate a random author ID
        byte[] id = new byte[32]; // 256-bit ID
        random.nextBytes(id);
        return new AuthorId(id);
    }
}
