package org.nodex.api.util;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Utility methods for string operations.
 */
@NotNullByDefault
public class StringUtils {
    
    private static final Random random = new Random();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    private StringUtils() {
        // Utility class
    }
    
    /**
     * Converts a string to UTF-8 bytes.
     */
    public static byte[] toUtf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Converts UTF-8 bytes to a string.
     */
    public static String fromUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Checks if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    /**
     * Truncates a string to a maximum length.
     */
    public static String truncate(String s, int maxLength) {
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength);
    }
    
    /**
     * Generates a random string of the specified length.
     */
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
