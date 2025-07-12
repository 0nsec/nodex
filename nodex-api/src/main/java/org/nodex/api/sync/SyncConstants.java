package org.nodex.api.sync;

/**
 * Constants related to synchronization
 */
public interface SyncConstants {
    
    /**
     * Maximum length of a message body in bytes
     */
    int MAX_MESSAGE_BODY_LENGTH = 1024 * 1024; // 1MB

    /**
     * Maximum number of messages to sync at once
     */
    int MAX_SYNC_MESSAGES = 100;

    /**
     * Maximum time to wait for sync response in milliseconds
     */
    long MAX_SYNC_TIMEOUT = 30000; // 30 seconds
}
