package org.nodex.api.system;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class FormatException extends Exception {
    
    public FormatException() {
        super();
    }
    
    public FormatException(String message) {
        super(message);
    }
    
    public FormatException(Throwable cause) {
        super(cause);
    }
    
    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
