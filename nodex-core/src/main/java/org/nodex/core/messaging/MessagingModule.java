package org.nodex.core.messaging;

import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageFactory;
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
 * Dagger module for the messaging feature.
 * Provides all messaging-related dependencies.
 */
@Module
public class MessagingModule {

    @Provides
    @Singleton
    PrivateMessageFactory providePrivateMessageFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new PrivateMessageFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    MessagingManager provideMessagingManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new MessagingManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }
}
