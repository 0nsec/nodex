package org.nodex.core.util;

import java.util.Collection;

public class ValidationUtils {
    
    public static void checkLength(String s, int maxLength) {
        if (s != null && s.length() > maxLength) {
            throw new IllegalArgumentException("String too long: " + s.length() + " > " + maxLength);
        }
    }
    
    public static void checkSize(Collection<?> collection, int maxSize) {
        if (collection != null && collection.size() > maxSize) {
            throw new IllegalArgumentException("Collection too large: " + collection.size() + " > " + maxSize);
        }
    }
    
    public static void checkRange(int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("Value out of range: " + value + " not in [" + min + ", " + max + "]");
        }
    }
    
    public static void checkNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    public static void checkNotEmpty(String s, String message) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
    
    public static void checkLength(String s, int minLength, int maxLength) {
        if (s != null) {
            if (s.length() < minLength) {
                throw new IllegalArgumentException("String too short: " + s.length() + " < " + minLength);
            }
            if (s.length() > maxLength) {
                throw new IllegalArgumentException("String too long: " + s.length() + " > " + maxLength);
            }
        }
    }
    
    public static void checkLength(byte[] bytes, int expectedLength) {
        if (bytes != null && bytes.length != expectedLength) {
            throw new IllegalArgumentException("Byte array wrong length: " + bytes.length + " != " + expectedLength);
        }
    }
    
    public static void checkSize(Collection<?> collection, int minSize, int maxSize) {
        if (collection != null) {
            if (collection.size() < minSize) {
                throw new IllegalArgumentException("Collection too small: " + collection.size() + " < " + minSize);
            }
            if (collection.size() > maxSize) {
                throw new IllegalArgumentException("Collection too large: " + collection.size() + " > " + maxSize);
            }
        }
    }
}
