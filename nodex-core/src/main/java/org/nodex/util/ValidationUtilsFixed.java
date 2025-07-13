package org.nodex.util;

import org.nodex.api.FormatException;
import org.nodex.api.data.BdfList;

import java.util.Collection;

public class ValidationUtilsFixed {
    
    // Original method
    public static void checkLength(String str, int minLength) throws FormatException {
        if (str == null || str.length() < minLength) {
            throw new FormatException("String too short");
        }
    }
    
    // Three-parameter version
    public static void checkLength(String str, int minLength, int maxLength) throws FormatException {
        if (str == null) {
            throw new FormatException("String is null");
        }
        if (str.length() < minLength || str.length() > maxLength) {
            throw new FormatException("String length out of range");
        }
    }
    
    // Single parameter for exact length
    public static void checkLength(byte[] data, int expectedLength) throws FormatException {
        if (data == null || data.length != expectedLength) {
            throw new FormatException("Invalid data length");
        }
    }
    
    // Collection size validation - single parameter
    public static void checkSize(Collection<?> collection, int expectedSize) throws FormatException {
        if (collection == null || collection.size() != expectedSize) {
            throw new FormatException("Invalid collection size");
        }
    }
    
    // Collection size validation - range
    public static void checkSize(Collection<?> collection, int minSize, int maxSize) throws FormatException {
        if (collection == null) {
            throw new FormatException("Collection is null");
        }
        int size = collection.size();
        if (size < minSize || size > maxSize) {
            throw new FormatException("Collection size out of range");
        }
    }
    
    // BdfList size validation - range
    public static void checkSize(BdfList list, int minSize, int maxSize) throws FormatException {
        if (list == null) {
            throw new FormatException("List is null");
        }
        int size = list.size();
        if (size < minSize || size > maxSize) {
            throw new FormatException("List size out of range");
        }
    }
    
    public static void checkRange(long value, long min, long max) throws FormatException {
        if (value < min || value > max) {
            throw new FormatException("Value out of range");
        }
    }
    
    public static void checkRange(int value, int min, int max) throws FormatException {
        if (value < min || value > max) {
            throw new FormatException("Value out of range");
        }
    }
    
    public static Long validateAutoDeleteTimer(Long timer) throws FormatException {
        if (timer != null && timer < 0) {
            throw new FormatException("Invalid auto-delete timer");
        }
        return timer;
    }
}
