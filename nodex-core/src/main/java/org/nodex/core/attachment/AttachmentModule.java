package org.nodex.core.attachment;

import org.nodex.api.attachment.AttachmentManager;
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
 * Dagger module for the attachment feature.
 * Provides all attachment-related dependencies.
 */
@Module
public class AttachmentModule {

    @Provides
    @Singleton
    AttachmentManager provideAttachmentManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new AttachmentManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }
}
