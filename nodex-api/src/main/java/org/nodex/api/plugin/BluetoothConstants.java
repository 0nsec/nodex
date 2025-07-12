package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for Bluetooth transport plugin.
 */
@NotNullByDefault
public class BluetoothConstants {
    
    public static final int UUID_BYTES = 16;
    public static final String BLUETOOTH_SERVICE_NAME = "NodeX";
    public static final int MAX_CONNECTIONS = 3;
    public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private BluetoothConstants() {
        // Utility class
    }
}
