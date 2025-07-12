package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for LAN TCP transport plugin.
 */
@NotNullByDefault
public class LanTcpConstants {
    
    public static final int DEFAULT_PORT = 7915;
    public static final int MAX_CONNECTIONS = 3;
    public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    public static final String DISCOVERY_SERVICE_NAME = "NodeX";
    
    private LanTcpConstants() {
        // Utility class
    }
}
