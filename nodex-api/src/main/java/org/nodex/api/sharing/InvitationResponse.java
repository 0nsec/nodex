package org.nodex.api.sharing;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Base class for invitation responses
 */
@NotNullByDefault
public abstract class InvitationResponse {
    private final boolean accepted;
    private final String message;

    protected InvitationResponse(boolean accepted, String message) {
        this.accepted = accepted;
        this.message = message;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }
}
