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
