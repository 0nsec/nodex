package org.nodex.api.util;

import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for logging.
 */
@NotNullByDefault
public class LogUtils {
    
    private LogUtils() {
        // Utility class
    }
    
    /**
     * Logs an exception with the specified logger and level.
     */
    public static void logException(Logger logger, Level level, Throwable t) {
        if (logger.isLoggable(level)) {
            logger.log(level, t.getMessage(), t);
        }
    }
    
    /**
     * Logs an exception with a custom message.
     */
    public static void logException(Logger logger, Level level, String message, Throwable t) {
        if (logger.isLoggable(level)) {
            logger.log(level, message, t);
        }
    }
}
