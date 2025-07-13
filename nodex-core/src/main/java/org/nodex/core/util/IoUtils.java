package org.nodex.core.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
    
    public static void copyAndClose(InputStream in, OutputStream out) throws IOException {
        try {
            copy(in, out);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Ignore
            }
            try {
                out.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
    
    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }
    
    /**
     * Try to close a closeable resource, ignoring any exceptions.
     */
    public static void tryToClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
