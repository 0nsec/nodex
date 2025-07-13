package org.nodex.api.plugin;

import org.nodex.api.plugin.duplex.DuplexPluginFactory;
import org.nodex.api.plugin.simplex.SimplexPluginFactory;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@NotNullByDefault
public interface PluginConfig {

	Collection<DuplexPluginFactory> getDuplexFactories();

	Collection<SimplexPluginFactory> getSimplexFactories();

	boolean shouldPoll();

	/**
	 * Returns a map representing transport preferences. For each entry in the
	 * map, connections via the transports identified by the value are
	 * preferred to connections via the transport identified by the key.
	 */
	Map<TransportId, List<TransportId>> getTransportPreferences();
}
