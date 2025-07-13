package org.nodex.api.settings;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

@NotNullByDefault
public interface SettingsManager extends Service {
    Settings getSettings(String namespace);
    void mergeSettings(Settings settings, String namespace);
}
