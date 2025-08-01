package org.nodex.api.plugin;

import org.nodex.api.Pair;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.transport.ConnectionHandler;
import org.nodex.api.settings.SettingsManager;
import org.nodex.api.system.Wakeful;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;

@NotNullByDefault
public interface Plugin {

	enum State {

		/**
		 * The plugin has not finished starting or has been stopped.
		 */
		STARTING_STOPPING,

		/**
		 * The plugin is disabled by settings. Use {@link #getReasonsDisabled()}
		 * to find out which settings are responsible.
		 */
		DISABLED,

		/**
		 * The plugin is being enabled and can't yet make or receive
		 * connections.
		 */
		ENABLING,

		/**
		 * The plugin is enabled and can make or receive connections.
		 */
		ACTIVE,

		/**
		 * The plugin is enabled but can't make or receive connections
		 */
		INACTIVE
	}

	/**
	 * The string for the boolean preference
	 * to use with the {@link SettingsManager} to enable or disable the plugin.
	 */
	String PREF_PLUGIN_ENABLE = "enable";

	/**
	 * Reason flag returned by {@link #getReasonsDisabled()} to indicate that
	 * the plugin has been disabled by the user.
	 */
	int REASON_USER = 1;

	/**
	 * Returns the plugin's transport identifier.
	 */
	TransportId getId();

	/**
	 * Returns the transport's maximum latency in milliseconds.
	 */
	long getMaxLatency();

	/**
	 * Returns the transport's maximum idle time in milliseconds.
	 */
	int getMaxIdleTime();

	/**
	 * Starts the plugin.
	 */
	@Wakeful
	void start() throws PluginException;

	/**
	 * Stops the plugin.
	 */
	@Wakeful
	void stop() throws PluginException;

	/**
	 * Returns the current state of the plugin.
	 */
	State getState();

	/**
	 * Returns a set of flags indicating why the plugin is
	 * {@link State#DISABLED disabled}, or 0 if the plugin is not disabled.
	 * <p>
	 * The flags used are plugin-specific, except the generic flag
	 * {@link #REASON_USER}, which may be used by any plugin.
	 */
	int getReasonsDisabled();

	/**
	 * Returns true if the plugin should be polled periodically to attempt to
	 * establish connections.
	 */
	boolean shouldPoll();

	/**
	 * Returns the desired interval in milliseconds between polling attempts.
	 */
	int getPollingInterval();

	/**
	 * Attempts to create connections using the given transport properties,
	 * passing any created connections to the corresponding handlers.
	 */
	@Wakeful
	void poll(Collection<Pair<TransportProperties, ConnectionHandler>>
			properties);
}
