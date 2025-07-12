package org.nodex.core.crypto;

import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.crypto.KeyPair;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.crypto.SecretKey;
import org.nodex.api.crypto.Signature;
import org.nodex.api.crypto.CryptoException;
import org.nodex.api.crypto.PasswordStrengthEstimator;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Implementation of CryptoComponent providing cryptographic operations.
 */
@ThreadSafe
@NotNullByDefault
public class CryptoComponentImpl implements CryptoComponent {

    private static final Logger LOG = Logger.getLogger(CryptoComponentImpl.class.getName());
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int KEY_SIZE = 256;
    private static final int SALT_LENGTH = 32;

    private final SecureRandom secureRandom;
    private final PasswordStrengthEstimator passwordStrengthEstimator;

    @Inject
    public CryptoComponentImpl(PasswordStrengthEstimator passwordStrengthEstimator) {
        this.secureRandom = new SecureRandom();
        this.passwordStrengthEstimator = passwordStrengthEstimator;
    }

    @Override
    public KeyPair generateKeyPair() {
        try {
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, secureRandom);
            java.security.KeyPair javaKeyPair = keyGen.generateKeyPair();
            
            PublicKey publicKey = new PublicKeyImpl(javaKeyPair.getPublic());
            PrivateKey privateKey = new PrivateKeyImpl(javaKeyPair.getPrivate());
            
            return new KeyPairImpl(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate key pair", e);
        }
    }

    @Override
    public SecretKey generateSecretKey() {
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        secureRandom.nextBytes(keyBytes);
        return new SecretKeyImpl(keyBytes);
    }

    @Override
    public byte[] hash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Hash algorithm not available", e);
        }
    }

    @Override
    public byte[] hash(byte[] input, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Hash algorithm not available", e);
        }
    }

    @Override
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    @Override
    public byte[] encrypt(byte[] plaintext, SecretKey key) throws CryptoException {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(key.getEncoded(), "AES");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, SecretKey key) throws CryptoException {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(key.getEncoded(), "AES");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    @Override
    public Signature sign(byte[] data, PrivateKey privateKey) throws CryptoException {
        try {
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            signature.initSign(((PrivateKeyImpl) privateKey).getJavaPrivateKey());
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            return new SignatureImpl(signatureBytes);
        } catch (Exception e) {
            throw new CryptoException("Signing failed", e);
        }
    }

    @Override
    public boolean verify(byte[] data, Signature signature, PublicKey publicKey) throws CryptoException {
        try {
            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
            verifier.initVerify(((PublicKeyImpl) publicKey).getJavaPublicKey());
            verifier.update(data);
            return verifier.verify(signature.getBytes());
        } catch (Exception e) {
            throw new CryptoException("Verification failed", e);
        }
    }

    @Override
    public SecretKey deriveKey(String password, byte[] salt) throws CryptoException {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, 100000, KEY_SIZE);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            javax.crypto.SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeyImpl(tmp.getEncoded());
        } catch (Exception e) {
            throw new CryptoException("Key derivation failed", e);
        }
    }

    @Override
    public float estimatePasswordStrength(String password) {
        return passwordStrengthEstimator.estimateStrength(password);
    }

    @Override
    public boolean isPasswordStrong(String password) {
        return passwordStrengthEstimator.estimateStrength(password) >= 0.5f;
    }

    @Override
    public byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    @Override
    public int getRandomInt(int bound) {
        return secureRandom.nextInt(bound);
    }

    @Override
    public long getRandomLong() {
        return secureRandom.nextLong();
    }
}
