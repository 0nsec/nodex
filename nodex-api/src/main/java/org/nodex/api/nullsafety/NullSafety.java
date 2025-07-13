package org.nodex.api.nullsafety;

import javax.annotation.Nullable;

public class NullSafety {
    
    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    public static int hashCode(@Nullable Object o) {
        return o == null ? 0 : o.hashCode();
    }
}
