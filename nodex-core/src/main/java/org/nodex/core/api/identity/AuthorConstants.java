package org.nodex.core.api.identity;

public class AuthorConstants {
    
    public static final int MAX_AUTHOR_NAME_LENGTH = 255;
    public static final int PUBLIC_KEY_LENGTH = 32; // For Ed25519
    public static final int PRIVATE_KEY_LENGTH = 32; // For Ed25519
    public static final int SIGNATURE_LENGTH = 64; // For Ed25519
    public static final int MAX_SIGNATURE_LENGTH = 64; // For Ed25519
    public static final int MAX_PUBLIC_KEY_LENGTH = 512; // Maximum public key length for all algorithms
    
    public static final String AUTHOR_NAME_KEY = "name";
    public static final String AUTHOR_PUBLIC_KEY = "publicKey";
    
    private AuthorConstants() {
        // Utility class
    }
}
