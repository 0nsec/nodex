package org.nodex.api.attachment;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;

@NotNullByDefault
public class AttachmentHeader {
    
    private final MessageId messageId;
    private final String contentType;
    private final long size;
    
    public AttachmentHeader(MessageId messageId, String contentType, long size) {
        this.messageId = messageId;
        this.contentType = contentType;
        this.size = size;
    }
    
    public MessageId getMessageId() {
        return messageId;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public long getSize() {
        return size;
    }
}
