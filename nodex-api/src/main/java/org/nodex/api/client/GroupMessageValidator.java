package org.nodex.api.client;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface GroupMessageValidator {
    boolean isValid(Object message);
}
