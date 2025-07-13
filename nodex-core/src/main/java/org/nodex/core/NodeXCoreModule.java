package org.nodex.core;

import org.nodex.core.forum.ForumModule;
import org.nodex.core.privategroup.PrivateGroupModule;
import org.nodex.core.avatar.AvatarModule;
import org.nodex.core.introduction.IntroductionModule;
import org.nodex.core.messaging.MessagingModule;
import org.nodex.core.feed.FeedModule;
import org.nodex.core.sharing.SharingModule;
import org.nodex.core.transport.TransportModule;
import org.nodex.core.transport.mailbox.MailboxModule;
import org.nodex.core.transport.bluetooth.BluetoothModule;
import org.nodex.core.transport.lan.LanModule;
import org.nodex.core.transport.tor.TorModule;
import org.nodex.core.crypto.CryptoModule;
import org.nodex.core.attachment.AttachmentModule;
import org.nodex.core.autodelete.AutoDeleteModule;
import org.nodex.core.identity.IdentityModule;

import dagger.Module;

/**
 * Main NodeX core module that aggregates all feature modules.
 * This provides a complete implementation of all Briar-compatible features.
 */
@Module(includes = {
    ForumModule.class,
    PrivateGroupModule.class,
    AvatarModule.class,
    IntroductionModule.class,
    MessagingModule.class,
    FeedModule.class,
    SharingModule.class,
    TransportModule.class,
    MailboxModule.class,
    BluetoothModule.class,
    LanModule.class,
    TorModule.class,
    CryptoModule.class,
    AttachmentModule.class,
    AutoDeleteModule.class,
    IdentityModule.class
})
public class NodeXCoreModule {
    // All functionality provided by included modules
}
