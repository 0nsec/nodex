package org.nodex.api.plugin.simplex;

import org.nodex.api.transport.TransportPlugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface SimplexPluginFactory {
    TransportPlugin createPlugin();
    String getId();
}
