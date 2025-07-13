package org.nodex.api.plugin.duplex;

import org.nodex.api.plugin.Plugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DuplexPluginFactory {
    Plugin createPlugin();
    String getId();
}
