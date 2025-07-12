package org.nodex.api.client;
import org.nodex.api.db.DbException;
public class ProtocolStateException extends DbException {
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