package org.nodex.api.messaging;

import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.conversation.ConversationMessageHeader;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

import java.util.List;

import javax.annotation.concurrent.Immutable;

/**
 * Header for a private message in a conversation
 */
@Immutable
@NotNullByDefault
public class PrivateMessageHeader extends ConversationMessageHeader {
    
    private final boolean hasText;
    private final List<AttachmentHeader> attachmentHeaders;

    public PrivateMessageHeader(MessageId id, GroupId groupId, long timestamp,
                               boolean local, boolean read, boolean sent, boolean seen,
                               boolean hasText, List<AttachmentHeader> headers,
                               long autoDeleteTimer) {
        super(id, groupId, timestamp, local, read, sent, seen, autoDeleteTimer);
        this.hasText = hasText;
        this.attachmentHeaders = headers;
    }

    public boolean hasText() {
        return hasText;
    }

    public List<AttachmentHeader> getAttachmentHeaders() {
        return attachmentHeaders;
    }

    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitPrivateMessageHeader(this);
    }
}
