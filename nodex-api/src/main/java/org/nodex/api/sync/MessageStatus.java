package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MessageStatus {
    MessageId getMessageId();
    boolean isSent();
    boolean isSeen();
}
