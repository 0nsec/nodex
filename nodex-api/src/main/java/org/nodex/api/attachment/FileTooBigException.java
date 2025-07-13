package org.nodex.api.attachment;

/**
 * Exception thrown when a file is too big to be processed.
 */
public class FileTooBigException extends Exception {
    
    public FileTooBigException() {
        super();
    }
    
    public FileTooBigException(String message) {
        super(message);
    }
    
    public FileTooBigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileTooBigException(Throwable cause) {
        super(cause);
    }
}
