package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for Tor transport plugin.
 */
@NotNullByDefault
public class TorConstants {
    
    public static final String TOR_VERSION = "4.7.13";
    public static final int SOCKS_PORT = 9050;
    public static final int CONTROL_PORT = 9051;
    public static final int MAX_CONNECTIONS = 3;
    public static final int CONNECTION_TIMEOUT = 60000; // 60 seconds
    public static final String ONION_SERVICE_PREFIX = "nodex";
    
    private TorConstants() {
        // Utility class
    }
}
