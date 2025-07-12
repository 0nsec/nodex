package org.nodex.api.messaging;

import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Message;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.emptyList;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;

/**
 * A private message between two contacts
 */
@Immutable
@NotNullByDefault
public class PrivateMessage {
    
    private final Message message;
    private final boolean hasText;
    private final List<AttachmentHeader> attachmentHeaders;
    private final long autoDeleteTimer;
    private final PrivateMessageFormat format;

    /**
     * Constructor for private messages in the TEXT_ONLY format.
     */
    public PrivateMessage(Message message) {
        this.message = message;
        this.hasText = true;
        this.attachmentHeaders = emptyList();
        this.autoDeleteTimer = NO_AUTO_DELETE_TIMER;
        this.format = PrivateMessageFormat.TEXT_ONLY;
    }

    /**
     * Constructor for private messages in the TEXT_IMAGES format.
     */
    public PrivateMessage(Message message, boolean hasText, List<AttachmentHeader> headers) {
        this.message = message;
        this.hasText = hasText;
        this.attachmentHeaders = headers;
        this.autoDeleteTimer = NO_AUTO_DELETE_TIMER;
        this.format = PrivateMessageFormat.TEXT_IMAGES;
    }

    /**
     * Constructor for private messages in the TEXT_IMAGES_AUTO_DELETE format.
     */
    public PrivateMessage(Message message, boolean hasText, 
                         List<AttachmentHeader> headers, long autoDeleteTimer) {
        this.message = message;
        this.hasText = hasText;
        this.attachmentHeaders = headers;
        this.autoDeleteTimer = autoDeleteTimer;
        this.format = PrivateMessageFormat.TEXT_IMAGES_AUTO_DELETE;
    }

    public Message getMessage() {
        return message;
    }

    public PrivateMessageFormat getFormat() {
        return format;
    }

    public boolean hasText() {
        return hasText;
    }

    public List<AttachmentHeader> getAttachmentHeaders() {
        return attachmentHeaders;
    }

    public long getAutoDeleteTimer() {
        return autoDeleteTimer;
    }
}
