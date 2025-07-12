package org.nodex.api.introduction;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Constants for introduction protocol - exact match to Briar.
 */
@NotNullByDefault
public class IntroductionConstants {
    
    // Protocol constants
    public static final String LABEL_ACTIVATE_MAC = "activate_mac";
    public static final String LABEL_ALICE_MAC_KEY = "alice_mac_key";
    public static final String LABEL_AUTH_MAC = "auth_mac";
    public static final String LABEL_AUTH_NONCE = "auth_nonce";
    public static final String LABEL_AUTH_SIGN = "auth_sign";
    public static final String LABEL_BOB_MAC_KEY = "bob_mac_key";
    public static final String LABEL_MASTER_KEY = "master_key";
    public static final String LABEL_SESSION_ID = "session_id";
    
    // Message limits
    public static final int MAX_INTRODUCTION_TEXT_LENGTH = 500;
    
    // Protocol timeouts
    public static final long PROTOCOL_TIMEOUT_MS = 60 * 1000; // 60 seconds
    
    private IntroductionConstants() {
        // Utility class
    }
}
