package org.nodex.nullsafety;

import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

/**
 * Null safety utilities - exact match to Briar.
 */
@NotNullByDefault
public class NullSafety {
    
    /**
     * Requires that the given object is not null.
     */
    public static <T> T requireNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException("Required object is null");
        }
        return obj;
    }
    
    /**
     * Requires that the given object is not null with a message.
     */
    public static <T> T requireNonNull(@Nullable T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }
    
    private NullSafety() {
        // Utility class
    }
}
