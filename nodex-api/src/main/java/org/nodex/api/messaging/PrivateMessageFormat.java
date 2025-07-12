package org.nodex.api.messaging;

/**
 * Enum representing different private message formats
 */
public enum PrivateMessageFormat {
    /**
     * First version of the private message format, which doesn't support
     * image attachments or auto-deletion.
     */
    TEXT_ONLY,

    /**
     * Second version of the private message format, which supports image
     * attachments but not auto-deletion.
     */
    TEXT_IMAGES,

    /**
     * Third version of the private message format, which supports image
     * attachments and auto-deletion.
     */
    TEXT_IMAGES_AUTO_DELETE
}
