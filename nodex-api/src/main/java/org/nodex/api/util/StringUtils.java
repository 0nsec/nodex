package org.nodex.api.util;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Collection;

@NotNullByDefault
public class StringUtils {
    
    public static byte[] toUtf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
    
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static String join(List<String> strings, String delimiter) {
        return String.join(delimiter, strings);
    }
    
    public static String join(Collection<String> strings, String delimiter) {
        return String.join(delimiter, strings);
    }
}
