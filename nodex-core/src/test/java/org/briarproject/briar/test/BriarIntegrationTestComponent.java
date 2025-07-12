package org.nodex.test;
import org.nodex.core.BrambleCoreIntegrationTestEagerSingletons;
import org.nodex.core.BrambleCoreModule;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.db.DatabaseComponent;
import org.nodex.core.api.identity.AuthorFactory;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.properties.TransportPropertyManager;
import org.nodex.core.api.system.Clock;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.test.BrambleCoreIntegrationTestModule;
import org.nodex.core.test.BrambleIntegrationTestComponent;
import org.nodex.core.test.TestDnsModule;
import org.nodex.core.test.TestPluginConfigModule;
import org.nodex.core.test.TestSocksModule;
import org.nodex.core.test.TimeTravel;
import org.nodex.api.attachment.AttachmentReader;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.introduction.IntroductionManager;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationFactory;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import org.nodex.attachment.AttachmentModule;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.blog.BlogModule;
import org.nodex.client.BriarClientModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.forum.ForumModule;
import org.nodex.identity.IdentityModule;
import org.nodex.introduction.IntroductionModule;
import org.nodex.messaging.MessagingModule;
import org.nodex.privategroup.PrivateGroupModule;
import org.nodex.privategroup.invitation.GroupInvitationModule;
import org.nodex.sharing.SharingModule;
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
		BriarClientModule.class,
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
public interface BriarIntegrationTestComponent
		extends BrambleIntegrationTestComponent {
	void inject(BriarIntegrationTest<BriarIntegrationTestComponent> init);
	void inject(AutoDeleteModule.EagerSingletons init);
	void inject(AvatarModule.EagerSingletons init);
	void inject(BlogModule.EagerSingletons init);
	void inject(ConversationModule.EagerSingletons init);
	void inject(ForumModule.EagerSingletons init);
	void inject(GroupInvitationModule.EagerSingletons init);
	void inject(IdentityModule.EagerSingletons init);
	void inject(IntroductionModule.EagerSingletons init);
	void inject(MessagingModule.EagerSingletons init);
	void inject(PrivateGroupModule.EagerSingletons init);
	void inject(SharingModule.EagerSingletons init);
	LifecycleManager getLifecycleManager();
	AttachmentReader getAttachmentReader();
	AvatarManager getAvatarManager();
	ContactManager getContactManager();
	ConversationManager getConversationManager();
	DatabaseComponent getDatabaseComponent();
	BlogManager getBlogManager();
	BlogSharingManager getBlogSharingManager();
	ForumSharingManager getForumSharingManager();
	ForumManager getForumManager();
	GroupInvitationManager getGroupInvitationManager();
	GroupInvitationFactory getGroupInvitationFactory();
	IntroductionManager getIntroductionManager();
	MessageTracker getMessageTracker();
	MessagingManager getMessagingManager();
	PrivateGroupManager getPrivateGroupManager();
	PrivateMessageFactory getPrivateMessageFactory();
	TransportPropertyManager getTransportPropertyManager();
	AuthorFactory getAuthorFactory();
	BlogFactory getBlogFactory();
	AutoDeleteManager getAutoDeleteManager();
	Clock getClock();
	TimeTravel getTimeTravel();
	class Helper {
		public static void injectEagerSingletons(
				BriarIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AutoDeleteModule.EagerSingletons());
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new ConversationModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new GroupInvitationModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new IntroductionModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
			c.inject(new PrivateGroupModule.EagerSingletons());
			c.inject(new SharingModule.EagerSingletons());
		}
	}
}