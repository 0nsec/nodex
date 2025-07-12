package org.nodex.core.test;

import java.util.Random;

/**
 * Utility methods for tests.
 */
public class TestUtils {
    
    private static final Random random = new Random();
    
    /**
     * Generates a random byte array of the specified length.
     */
    public static byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generates a random ID (32 bytes).
     */
    public static byte[] getRandomId() {
        return getRandomBytes(32);
    }
    
    /**
     * Generates a random string of the specified length.
     */
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
}
