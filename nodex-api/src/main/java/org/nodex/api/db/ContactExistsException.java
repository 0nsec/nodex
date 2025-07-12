package org.nodex.api.db;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Exception thrown when attempting to add a contact that already exists.
 */
@NotNullByDefault
public class ContactExistsException extends DbException {

    public ContactExistsException() {
        super("Contact already exists");
    }

    public ContactExistsException(String message) {
        super(message);
    }

    public ContactExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContactExistsException(Throwable cause) {
        super(cause);
    }
}
