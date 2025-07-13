package org.nodex.core.blog;

import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPostFactory;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.BlogPostValidator;
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
 * Dagger module for the blog feature.
 * Provides all blog-related dependencies.
 */
@Module
public class BlogModule {

    @Provides
    @Singleton
    BlogFactory provideBlogFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new BlogFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    BlogPostFactory provideBlogPostFactory(
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager,
            MetadataEncoder metadataEncoder
    ) {
        return new BlogPostFactoryImpl(clientHelper, clock, identityManager, metadataEncoder);
    }

    @Provides
    @Singleton
    BlogManager provideBlogManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new BlogManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }

    @Provides
    @Singleton
    BlogPostValidator provideBlogPostValidator(
            MetadataParser metadataParser,
            ClientHelper clientHelper,
            Clock clock
    ) {
        return new BlogPostValidator(metadataParser, clientHelper, clock);
    }
}
