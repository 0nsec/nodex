package org.nodex.api.db;

/**
 * Exception thrown when a protocol state is invalid.
 */
public class ProtocolStateException extends DbException {
    public ProtocolStateException() {
        super();
    }

    public ProtocolStateException(String message) {
        super(message);
    }

    public ProtocolStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolStateException(Throwable cause) {
        super(cause);
    }
}
