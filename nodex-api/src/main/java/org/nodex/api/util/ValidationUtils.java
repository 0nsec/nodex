package org.nodex.api.util;

import org.nodex.api.FormatException;
import org.nodex.api.data.BdfList;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Utility methods for validation.
 */
@NotNullByDefault
public class ValidationUtils {
    
    private ValidationUtils() {
        // Utility class
    }
    
    /**
     * Checks that a BDF list has the expected size.
     */
    public static void checkSize(BdfList list, int expectedSize) throws FormatException {
        if (list.size() != expectedSize) {
            throw new FormatException("Expected " + expectedSize + " elements, got " + list.size());
        }
    }
    
    /**
     * Checks that a byte array has the expected length.
     */
    public static void checkLength(byte[] array, int expectedLength) {
        if (array.length != expectedLength) {
            throw new IllegalArgumentException("Expected length " + expectedLength + ", got " + array.length);
        }
    }
    
    /**
     * Checks that a string has a length within the given range.
     */
    public static void checkLength(String s, int minLength, int maxLength) {
        if (s.length() < minLength || s.length() > maxLength) {
            throw new IllegalArgumentException("String length must be between " + minLength + " and " + maxLength);
        }
    }
    
    /**
     * Checks that a value is within the given range.
     */
    public static void checkRange(int value, int minValue, int maxValue) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException("Value must be between " + minValue + " and " + maxValue);
        }
    }
}
