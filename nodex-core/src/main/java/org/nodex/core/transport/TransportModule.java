package org.nodex.core.transport;

import org.nodex.api.transport.TransportManager;
import org.nodex.api.transport.TransportPlugin;
import org.nodex.api.transport.KeyManager;
import org.nodex.api.transport.ConnectionManager;
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
 * Dagger module for the transport system.
 * Provides all transport-related dependencies.
 */
@Module
public class TransportModule {

    @Provides
    @Singleton
    TransportManager provideTransportManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new TransportManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }

    @Provides
    @Singleton
    KeyManager provideKeyManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new KeyManagerImpl(db, clientHelper, clock, identityManager);
    }

    @Provides
    @Singleton
    ConnectionManager provideConnectionManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new ConnectionManagerImpl(db, clientHelper, clock, identityManager);
    }
}
