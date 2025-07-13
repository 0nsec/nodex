package org.nodex.api.attachment;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;
import java.util.Collection;

/**
 * Manages file attachments in messages.
 */
@NotNullByDefault
public interface AttachmentManager extends Service {
    
    /**
     * Store attachments for a message.
     */
    void storeAttachments(Collection<String> attachmentPaths);
    
    /**
     * Get attachment headers for sending.
     */
    Collection<String> getAttachmentHeaders();
    
    /**
     * Delete an attachment.
     */
    void deleteAttachment(String attachmentId);
    
    /**
     * Get attachment data.
     */
    byte[] getAttachmentData(String attachmentId);
}
