package org.nodex.api.sync.impl;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.MessageStatus;

@NotNullByDefault
public class MessageStatusImpl implements MessageStatus {
    private final MessageId id;
    private final boolean sent;
    private final boolean seen;

    public MessageStatusImpl(MessageId id, boolean sent, boolean seen) {
        this.id = id; this.sent = sent; this.seen = seen;
    }
    @Override public MessageId getMessageId() { return id; }
    @Override public boolean isSent() { return sent; }
    @Override public boolean isSeen() { return seen; }
}
