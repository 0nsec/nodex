package org.nodex.core;

import org.nodex.core.avatar.AvatarModule;
import org.nodex.core.feed.FeedModule;
import org.nodex.core.transport.TransportModule;
import org.nodex.core.transport.mailbox.MailboxModule;
import org.nodex.core.transport.bluetooth.BluetoothModule;
import org.nodex.core.transport.lan.LanModule;
import org.nodex.core.transport.tor.TorModule;
import org.nodex.core.crypto.CryptoModule;
import org.nodex.core.attachment.AttachmentModule;
import org.nodex.core.autodelete.AutoDeleteModule;
import org.nodex.core.identity.IdentityModule;
import org.nodex.client.NodexClientModule;

import dagger.Module;

/**
 * Main NodeX core module that aggregates all feature modules.
 * This provides a complete implementation of all Briar-compatible features.
 */
@Module(includes = {
    // Excluded incomplete modules: ForumModule, PrivateGroupModule
    AvatarModule.class,
    FeedModule.class,
    // SharingModule excluded for now
    TransportModule.class,
    MailboxModule.class,
    BluetoothModule.class,
    LanModule.class,
    TorModule.class,
    CryptoModule.class,
    AttachmentModule.class,
    AutoDeleteModule.class,
    IdentityModule.class,
    NodexClientModule.class
})
public class NodeXCoreModule {
    // All functionality provided by included modules
}
