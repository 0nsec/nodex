package org.nodex.api;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Utility class for byte array operations.
 */
@Immutable
@NotNullByDefault
public class Bytes {
    
    public static final int MAX_32_BIT_UNSIGNED = 0x7FFFFFFF;
    
    private Bytes() {} // Utility class
    
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static boolean arraysEqual(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }
}
