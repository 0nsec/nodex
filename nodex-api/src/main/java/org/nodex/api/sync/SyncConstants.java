package org.nodex.api.sync;

/**
 * Constants for synchronization.
 */
public class SyncConstants {
    
    /**
     * The length of a message header in bytes.
     */
    public static final int MESSAGE_HEADER_LENGTH = 36;
    
    /**
     * The maximum length of a group descriptor in bytes.
     */
    public static final int MAX_GROUP_DESCRIPTOR_LENGTH = 1024;
    
    /**
     * The maximum length of a message body in bytes.
     */
    public static final int MAX_MESSAGE_BODY_LENGTH = 1024 * 1024; // 1MB
    
    /**
     * The maximum length of an author name in UTF-8 bytes.
     */
    public static final int MAX_AUTHOR_NAME_LENGTH = 255;
    
    /**
     * Maximum number of messages to sync at once
     */
    public static final int MAX_SYNC_MESSAGES = 100;

    /**
     * Maximum time to wait for sync response in milliseconds
     */
    public static final long MAX_SYNC_TIMEOUT = 30000; // 30 seconds
    
    private SyncConstants() {
        // Utility class
    }
}
