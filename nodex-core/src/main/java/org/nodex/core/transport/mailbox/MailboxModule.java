package org.nodex.core.transport.mailbox;

import org.nodex.api.transport.mailbox.MailboxManager;
import org.nodex.api.transport.mailbox.MailboxTransportPlugin;
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
 * Dagger module for the mailbox transport plugin.
 * Provides all mailbox-related dependencies.
 */
@Module
public class MailboxModule {

    @Provides
    @Singleton
    MailboxTransportPlugin provideMailboxTransportPlugin(
            DatabaseComponent db,
            ClientHelper clientHelper,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new MailboxTransportPluginImpl(db, clientHelper, clock, identityManager);
    }

    @Provides
    @Singleton
    MailboxManager provideMailboxManager(
            DatabaseComponent db,
            ClientHelper clientHelper,
            ClientVersioningManager clientVersioningManager,
            MetadataParser metadataParser,
            MetadataEncoder metadataEncoder,
            Clock clock,
            IdentityManager identityManager
    ) {
        return new MailboxManagerImpl(db, clientHelper, clientVersioningManager, 
                metadataParser, metadataEncoder, clock, identityManager);
    }
}
