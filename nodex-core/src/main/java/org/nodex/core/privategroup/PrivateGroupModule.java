package org.nodex.core.privategroup;

import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.ValidationManager;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.data.MetadataParser;
import org.nodex.api.system.Clock;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.privategroup.GroupMessageValidator;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for the private group feature.
 * Provides all private group-related dependencies.
 */
@Module
public class PrivateGroupModule {

    @Provides
    @Singleton
    PrivateGroupFactory providePrivateGroupFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new PrivateGroupFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    GroupMessageFactory provideGroupMessageFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new GroupMessageFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    PrivateGroupManager providePrivateGroupManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new PrivateGroupManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }

    @Provides
    @Singleton
    GroupMessageValidator provideGroupMessageValidator(
            MetadataParser metadataParser,
            ClientHelper clientHelper,
            Clock clock
    ) {
        return new GroupMessageValidator(metadataParser, clientHelper, clock);
    }
}
