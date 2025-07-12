package org.nodex.messaging;
import org.nodex.api.FeatureFlags;
import org.nodex.api.cleanup.CleanupManager;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.data.BdfReaderFactory;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.validation.ValidationManager;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.messaging.MessagingManager;
import org.nodex.api.messaging.PrivateMessageFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static org.nodex.api.messaging.MessagingManager.CLIENT_ID;
import static org.nodex.api.messaging.MessagingManager.MAJOR_VERSION;
import static org.nodex.api.messaging.MessagingManager.MINOR_VERSION;
@Module
public class MessagingModule {
	public static class EagerSingletons {
		@Inject
		MessagingManager messagingManager;
		@Inject
		PrivateMessageValidator privateMessageValidator;
	}
	@Provides
	PrivateMessageFactory providePrivateMessageFactory(
			PrivateMessageFactoryImpl privateMessageFactory) {
		return privateMessageFactory;
	}
	@Provides
	@Singleton
	PrivateMessageValidator getValidator(ValidationManager validationManager,
			BdfReaderFactory bdfReaderFactory, MetadataEncoder metadataEncoder,
			Clock clock) {
		PrivateMessageValidator validator = new PrivateMessageValidator(
				bdfReaderFactory, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}
	@Provides
	@Singleton
	MessagingManager getMessagingManager(LifecycleManager lifecycleManager,
			ContactManager contactManager, ValidationManager validationManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags,
			MessagingManagerImpl messagingManager) {
		lifecycleManager.registerOpenDatabaseHook(messagingManager);
		contactManager.registerContactHook(messagingManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		conversationManager.registerConversationClient(messagingManager);
		boolean images = featureFlags.shouldEnableImageAttachments();
		boolean disappear = featureFlags.shouldEnableDisappearingMessages();
		int minorVersion = images ? (disappear ? MINOR_VERSION : 2) : 0;
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				minorVersion, messagingManager);
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		return messagingManager;
	}
}