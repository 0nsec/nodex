package org.nodex.api.plugin.simplex;

import org.nodex.api.plugin.Plugin;
import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface SimplexPluginFactory {
    Plugin createPlugin();
    String getId();
}
