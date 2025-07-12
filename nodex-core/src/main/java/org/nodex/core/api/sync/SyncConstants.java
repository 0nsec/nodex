package org.nodex.core.api.sync;

public class SyncConstants {
    
    public static final int MAX_MESSAGE_BODY_LENGTH = 1024 * 1024; // 1MB
    public static final int MAX_MESSAGE_SIZE = MAX_MESSAGE_BODY_LENGTH + 1024; // Extra for headers
    public static final int MAX_GROUP_DESCRIPTOR_LENGTH = 1024;
    public static final int MAX_AUTHOR_NAME_LENGTH = 255;
    
    public static final String MESSAGE_HEADER_TYPE = "TYPE";
    public static final String MESSAGE_HEADER_TIMESTAMP = "TIMESTAMP";
    public static final String MESSAGE_HEADER_AUTHOR = "AUTHOR";
    
    private SyncConstants() {
        // Utility class
    }
}
