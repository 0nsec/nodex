package org.nodex.core.util;

import java.util.logging.Logger;

public class LogUtils {
    
    private static final Logger LOG = Logger.getLogger(LogUtils.class.getName());
    
    public static long now() {
        return System.currentTimeMillis();
    }
    
    public static void logDuration(Logger logger, String operation, long startTime) {
        long duration = now() - startTime;
        logger.info(operation + " took " + duration + " ms");
    }
    
    public static void logException(Logger logger, Exception e, String context) {
        logger.severe(context + ": " + e.getMessage());
        if (logger.isLoggable(java.util.logging.Level.FINE)) {
            e.printStackTrace();
        }
    }
}
