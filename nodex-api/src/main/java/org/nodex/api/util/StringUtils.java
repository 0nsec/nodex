package org.nodex.api.util;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.nio.charset.StandardCharsets;

/**
 * Utility methods for string operations.
 */
@NotNullByDefault
public class StringUtils {
    
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
}
