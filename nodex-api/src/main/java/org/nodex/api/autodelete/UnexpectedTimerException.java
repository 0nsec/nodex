package org.nodex.api.autodelete;
import org.nodex.api.db.DbException;
public class UnexpectedTimerException extends DbException {
    public UnexpectedTimerException(String message) {
        super(message);
    }

    public UnexpectedTimerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedTimerException(Throwable cause) {
        super(cause);
    }
}