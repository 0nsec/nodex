package org.nodex.api.plugin.duplex;

import org.nodex.api.transport.TransportPlugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DuplexPluginFactory {
    TransportPlugin createPlugin();
    String getId();
}
