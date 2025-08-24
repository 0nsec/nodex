package org.nodex.api;

import org.nodex.nullsafety.NotNullByDefault;

/**
 * Feature flags for enabling/disabling experimental features.
 */
@NotNullByDefault
public interface FeatureFlags {
    /**
     * Check if a feature is enabled.
     */
    boolean isFeatureEnabled(String featureName);
    
    /**
     * Check if image attachments are enabled.
     */
    boolean shouldEnableImageAttachments();
    
    /**
     * Check if disappearing messages are enabled.
     */
    boolean shouldEnableDisappearingMessages();

    // Added legacy flags used by core modules (temporary default implementations may always return false in impl)
    default boolean shouldEnableBlogsInCore() { return false; }
    default boolean shouldEnableForumsInCore() { return false; }
    default boolean shouldEnablePrivateGroupsInCore() { return false; }
}
