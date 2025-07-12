package org.nodex.api.test;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Interface for creating test avatars.
 */
@NotNullByDefault
public interface TestAvatarCreator {
    
    /**
     * Creates a test avatar image.
     */
    byte[] createAvatarImage();
    
    /**
     * Creates a test avatar image with specific dimensions.
     */
    byte[] createAvatarImage(int width, int height);
}
