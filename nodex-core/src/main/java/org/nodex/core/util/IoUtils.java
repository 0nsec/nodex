package org.nodex.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Logger;

public class IoUtils {
    
    private static final Logger LOG = Logger.getLogger(IoUtils.class.getName());
    
    public static void tryToClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.warning("Failed to close resource: " + e.getMessage());
            }
        }
    }
    
    public static void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            tryToClose(closeable);
        }
    }
}
