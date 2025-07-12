package org.nodex.api.messaging;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Constants for messaging system.
 */
@NotNullByDefault
public interface MessagingConstants {
    int MAX_PRIVATE_MESSAGE_TEXT_LENGTH = 8192;
    int MAX_ATTACHMENTS_PER_MESSAGE = 10;
    int MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB
}
