package org.nodex.api.sharing;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base class for invitation requests
 */
@NotNullByDefault
public abstract class InvitationRequest<T extends Shareable> {
    private final T shareable;
    private final String message;

    protected InvitationRequest(T shareable, String message) {
        this.shareable = shareable;
        this.message = message;
    }

    public T getShareable() {
        return shareable;
    }

    public String getMessage() {
        return message;
    }
}
