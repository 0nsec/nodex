package org.nodex.core.api.transport;

public class TransportConstants {
    
    public static final long MAX_CLOCK_DIFFERENCE = 5 * 60 * 1000; // 5 minutes in milliseconds
    public static final int MAX_PACKET_SIZE = 64 * 1024; // 64KB
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30 * 1000; // 30 seconds
    public static final int DEFAULT_READ_TIMEOUT = 60 * 1000; // 60 seconds
    
    public static final String TRANSPORT_ID_BLUETOOTH = "bluetooth";
    public static final String TRANSPORT_ID_LAN = "lan";
    public static final String TRANSPORT_ID_TOR = "tor";
    public static final String TRANSPORT_ID_MAILBOX = "mailbox";
    
    private TransportConstants() {
        // Utility class
    }
}
