package org.nodex.api.transport;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConnectionHandler {
    void handleConnection();
    void closeConnection();
}
