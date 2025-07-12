package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Cryptographic component for NodeX.
 */
@NotNullByDefault
public interface CryptoComponent {
    
    /**
     * Generates a new key pair.
     */
    KeyPair generateKeyPair();
    
    /**
     * Generates a new secret key.
     */
    SecretKey generateSecretKey();
    
    /**
     * Signs data with the given private key.
     */
    byte[] sign(byte[] data, PrivateKey privateKey);
    
    /**
     * Verifies a signature with the given public key.
     */
    boolean verify(byte[] data, byte[] signature, PublicKey publicKey);
    
    /**
     * Encrypts data with the given secret key.
     */
    byte[] encrypt(byte[] data, SecretKey secretKey);
    
    /**
     * Decrypts data with the given secret key.
     */
    byte[] decrypt(byte[] encryptedData, SecretKey secretKey);
    
    /**
     * Computes a hash of the given data.
     */
    byte[] hash(byte[] data);
}
