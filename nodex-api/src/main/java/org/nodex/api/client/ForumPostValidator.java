package org.nodex.api.client;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ForumPostValidator {
    boolean isValid(Object forumPost);
}
