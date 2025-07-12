package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for Tor transport plugin.
 */
@NotNullByDefault
public class TorConstants {
    
    public static final TransportId ID = new TransportId("org.nodex.tor");
    
    public static final String PROP_ONION_V3 = "onion3";
    
    public static final String TOR_VERSION = "4.7.13";
    public static final int SOCKS_PORT = 9050;
    public static final int CONTROL_PORT = 9051;
    public static final int MAX_CONNECTIONS = 3;
    public static final int CONNECTION_TIMEOUT = 60000; // 60 seconds
    public static final String ONION_SERVICE_PREFIX = "nodex";
    
    // Tor network preferences
    public static final int PREF_TOR_NETWORK_WITHOUT_BRIDGES = 1;
    public static final int PREF_TOR_NETWORK_WITH_BRIDGES = 2;
    
    private TorConstants() {
        // Utility class
    }
}
