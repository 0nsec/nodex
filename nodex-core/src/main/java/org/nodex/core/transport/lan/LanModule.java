package org.nodex.core.transport.lan;

import org.nodex.api.transport.lan.LanTransportPlugin;
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
 * Dagger module for the LAN transport plugin.
 * Provides all LAN-related dependencies.
 */
@Module
public class LanModule {

    @Provides
    @Singleton
    LanTransportPlugin provideLanTransportPlugin(
            DatabaseComponent db,
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new LanTransportPluginImpl(db, clientHelper, clock, identityManager);
    }
}
