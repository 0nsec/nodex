package org.nodex.core.forum;

import org.nodex.api.forum.ForumFactory;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumPostFactory;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ForumPostValidator;
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
 * Dagger module for the forum feature.
 * Provides all forum-related dependencies.
 */
@Module
public class ForumModule {

    @Provides
    @Singleton
    ForumFactory provideForumFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new ForumFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    ForumPostFactory provideForumPostFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new ForumPostFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    ForumManager provideForumManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new ForumManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }

    @Provides
    @Singleton
    ForumPostValidator provideForumPostValidator(
            MetadataParser metadataParser,
            ClientHelper clientHelper,
            Clock clock
    ) {
        return new ForumPostValidator(metadataParser, clientHelper, clock);
    }
}
