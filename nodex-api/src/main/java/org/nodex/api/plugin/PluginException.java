package org.nodex.api.plugin;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public class PluginException extends Exception {
    public PluginException() {}
    public PluginException(String message) { super(message); }
    public PluginException(Throwable cause) { super(cause); }
    public PluginException(String message, Throwable cause) { super(message, cause); }
}
