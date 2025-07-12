package org.nodex.messaging;
import org.nodex.core.BrambleCoreIntegrationTestEagerSingletons;
import org.nodex.core.BrambleCoreModule;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.test.BrambleCoreIntegrationTestModule;
import org.nodex.core.test.TestDnsModule;
import org.nodex.core.test.TestPluginConfigModule;
import org.nodex.core.test.TestSocksModule;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.avatar.AvatarModule;
import org.nodex.client.BriarClientModule;
import org.nodex.conversation.ConversationModule;
import org.nodex.forum.ForumModule;
import org.nodex.identity.IdentityModule;
import javax.inject.Singleton;
import dagger.Component;
@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		BriarClientModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		ConversationModule.class,
		ForumModule.class,
		IdentityModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface MessageSizeIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {
	void inject(MessageSizeIntegrationTest testCase);
	void inject(AvatarModule.EagerSingletons init);
	void inject(ForumModule.EagerSingletons init);
	void inject(MessagingModule.EagerSingletons init);
	class Helper {
		public static void injectEagerSingletons(
				MessageSizeIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}