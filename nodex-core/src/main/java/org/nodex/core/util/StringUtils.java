package org.nodex.core.util;

public class StringUtils {
    
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static String getRandomString(int length) {
        return org.nodex.api.util.StringUtils.getRandomString(length);
    }
    
    public static String truncate(String str, int maxLength) {
        return org.nodex.api.util.StringUtils.truncate(str, maxLength);
    }
}

    public static boolean utf8IsTooLong(String str, int maxBytes) {
        if (str == null) return false;
        return str.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maxBytes;
    }
    
    public static String truncateUtf8(String str, int maxBytes) {
        if (str == null) return null;
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return str;
        
        // Truncate at character boundary
        int truncatePoint = maxBytes;
        while (truncatePoint > 0 && (bytes[truncatePoint] & 0x80) != 0 && (bytes[truncatePoint] & 0x40) == 0) {
            truncatePoint--;
        }
        return new String(bytes, 0, truncatePoint, java.nio.charset.StandardCharsets.UTF_8);
    }
