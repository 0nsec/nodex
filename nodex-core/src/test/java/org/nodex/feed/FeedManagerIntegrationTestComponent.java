package org.nodex.feed;
import org.nodex.core.BrambleCoreIntegrationTestEagerSingletons;
import org.nodex.core.BrambleCoreModule;
import org.nodex.api.identity.IdentityManager;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.test.BrambleCoreIntegrationTestModule;
import org.nodex.core.test.TestDnsModule;
import org.nodex.core.test.TestPluginConfigModule;
import org.nodex.core.test.TestSocksModule;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.feed.FeedManager;
import org.nodex.avatar.AvatarModule;
import org.nodex.blog.BlogModule;
import org.nodex.client.NodexClientModule;
import org.nodex.identity.IdentityModule;
import javax.inject.Singleton;
import dagger.Component;
@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		AvatarModule.class,
		BlogModule.class,
		NodexClientModule.class,
		FeedModule.class,
		IdentityModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface FeedManagerIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {
	void inject(FeedManagerIntegrationTest testCase);
	void inject(AvatarModule.EagerSingletons init);
	void inject(BlogModule.EagerSingletons init);
	void inject(FeedModule.EagerSingletons init);
	IdentityManager getIdentityManager();
	LifecycleManager getLifecycleManager();
	FeedManager getFeedManager();
	BlogManager getBlogManager();
	class Helper {
		public static void injectEagerSingletons(
				FeedManagerIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
		}
	}
}