package org.nodex.api.messaging;

import org.nodex.api.system.FormatException;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Factory for creating private messages
 */
@NotNullByDefault
public interface PrivateMessageFactory {
    
    /**
     * Creates a private message in the TEXT_ONLY format.
     */
    PrivateMessage createLegacyPrivateMessage(GroupId groupId, long timestamp,
                                             String text) throws FormatException;

    /**
     * Creates a private message in the TEXT_IMAGES format.
     */
    PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
                                       @Nullable String text, List<AttachmentHeader> headers)
                                       throws FormatException;

    /**
     * Creates a private message in the TEXT_IMAGES_AUTO_DELETE format.
     */
    PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
                                       @Nullable String text, List<AttachmentHeader> headers,
                                       long autoDeleteTimer) throws FormatException;
}
