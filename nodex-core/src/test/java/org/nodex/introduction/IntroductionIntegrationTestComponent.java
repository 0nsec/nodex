package org.nodex.introduction;
import org.nodex.core.BrambleCoreModule;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.test.BrambleCoreIntegrationTestModule;
import org.nodex.core.test.TestDnsModule;
import org.nodex.core.test.TestPluginConfigModule;
import org.nodex.core.test.TestSocksModule;
import org.nodex.attachment.AttachmentModule;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.blog.BlogModule;
import org.nodex.client.NodexClientModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.forum.ForumModule;
import org.nodex.identity.IdentityModule;
import org.nodex.messaging.MessagingModule;
import org.nodex.privategroup.PrivateGroupModule;
import org.nodex.privategroup.invitation.GroupInvitationModule;
import org.nodex.sharing.SharingModule;
import org.nodex.test.NodexIntegrationTestComponent;
import javax.inject.Singleton;
import dagger.Component;
@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		AttachmentModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		BlogModule.class,
		NodexClientModule.class,
		ConversationModule.class,
		ForumModule.class,
		GroupInvitationModule.class,
		IdentityModule.class,
		IntroductionModule.class,
		MessagingModule.class,
		PrivateGroupModule.class,
		SharingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface IntroductionIntegrationTestComponent
		extends NodexIntegrationTestComponent {
	void inject(IntroductionIntegrationTest init);
	void inject(MessageEncoderParserIntegrationTest init);
	void inject(SessionEncoderParserIntegrationTest init);
	void inject(IntroductionCryptoIntegrationTest init);
	void inject(AutoDeleteIntegrationTest init);
	MessageEncoder getMessageEncoder();
	MessageParser getMessageParser();
	SessionParser getSessionParser();
	IntroductionCrypto getIntroductionCrypto();
}
