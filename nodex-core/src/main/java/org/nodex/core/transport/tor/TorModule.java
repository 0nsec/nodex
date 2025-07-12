package org.nodex.core.transport.tor;

import org.nodex.api.transport.tor.TorTransportPlugin;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.ValidationManager;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.system.Clock;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.db.DatabaseComponent;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for the Tor transport plugin.
 * Provides all Tor-related dependencies.
 */
@Module
public class TorModule {

    @Provides
    @Singleton
    TorTransportPlugin provideTorTransportPlugin(
            DatabaseComponent db,
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new TorTransportPluginImpl(db, clientHelper, clock, identityManager);
    }
}
