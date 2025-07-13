package org.nodex.api.system;

import java.io.IOException;

/**
 * An exception that indicates an unrecoverable formatting error.
 */
public class FormatException extends IOException {
    
    public FormatException() {
        super();
    }
    
    public FormatException(String message) {
        super(message);
    }
    
    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FormatException(Throwable cause) {
        super(cause);
    }
}
