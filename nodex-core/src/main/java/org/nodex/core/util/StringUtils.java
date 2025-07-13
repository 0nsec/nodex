package org.nodex.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class StringUtils {
    
    private static final Random random = new Random();
    
    public static boolean utf8IsTooLong(String s, int maxLength) {
        if (s == null) return false;
        return s.getBytes(StandardCharsets.UTF_8).length > maxLength;
    }
    
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
    
    /**
     * Check if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    
    /**
     * Check if a string is null, empty, or contains only whitespace.
     */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    public static String truncateUtf8(String s, int maxBytes) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return s;
        
        // Find the boundary to avoid cutting in the middle of a character
        int i = maxBytes;
        while (i > 0 && (bytes[i] & 0x80) != 0 && (bytes[i] & 0xC0) != 0xC0) {
            i--;
        }
        return new String(bytes, 0, i, StandardCharsets.UTF_8);
    }
}
