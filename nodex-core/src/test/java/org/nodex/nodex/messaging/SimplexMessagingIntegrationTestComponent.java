package org.nodex.messaging;
import org.nodex.core.BrambleCoreIntegrationTestEagerSingletons;
import org.nodex.core.BrambleCoreModule;
import org.nodex.core.api.connection.ConnectionManager;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.mailbox.ModularMailboxModule;
import org.nodex.core.test.BrambleCoreIntegrationTestModule;
import org.nodex.core.test.TestDnsModule;
import org.nodex.core.test.TestPluginConfigModule;
import org.nodex.core.test.TestSocksModule;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.autodelete.AutoDeleteModule;
import org.nodex.client.NodexClientModule;
import org.nodex.conversation.ConversationModule;
import javax.inject.Singleton;
import dagger.Component;
@Singleton
@Component(modules = {
		AutoDeleteModule.class,
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		NodexClientModule.class,
		ConversationModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface SimplexMessagingIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {
	void inject(MessagingModule.EagerSingletons init);
	LifecycleManager getLifecycleManager();
	IdentityManager getIdentityManager();
	ContactManager getContactManager();
	MessagingManager getMessagingManager();
	PrivateMessageFactory getPrivateMessageFactory();
	EventBus getEventBus();
	ConnectionManager getConnectionManager();
	class Helper {
		public static void injectEagerSingletons(
				SimplexMessagingIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}