package org.nodex.api.client;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault  
public interface BlogPostValidator {
    boolean isValid(Object blogPost);
}
