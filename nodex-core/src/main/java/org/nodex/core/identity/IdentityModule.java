package org.nodex.core.identity;

import org.nodex.api.identity.IdentityManager;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.ValidationManager;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.system.Clock;
import org.nodex.api.db.DatabaseComponent;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for the identity management feature.
 * Provides all identity-related dependencies.
 */
@Module
public class IdentityModule {

    @Provides
    @Singleton
    IdentityManager provideIdentityManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock
    ) {
        return new IdentityManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock);
    }
}
