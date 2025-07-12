package org.nodex.introduction;
import org.nodex.core.api.cleanup.CleanupManager;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.data.MetadataEncoder;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.validation.ValidationManager;
import org.nodex.core.api.system.Clock;
import org.nodex.core.api.versioning.ClientVersioningManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.introduction.IntroductionManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static org.nodex.api.introduction.IntroductionManager.CLIENT_ID;
import static org.nodex.api.introduction.IntroductionManager.MAJOR_VERSION;
import static org.nodex.api.introduction.IntroductionManager.MINOR_VERSION;
@Module
public class IntroductionModule {
	public static class EagerSingletons {
		@Inject
		IntroductionValidator introductionValidator;
		@Inject
		IntroductionManager introductionManager;
	}
	@Provides
	@Singleton
	IntroductionValidator provideValidator(ValidationManager validationManager,
			MessageEncoder messageEncoder, MetadataEncoder metadataEncoder,
			ClientHelper clientHelper, Clock clock) {
		IntroductionValidator introductionValidator =
				new IntroductionValidator(messageEncoder, clientHelper,
						metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				introductionValidator);
		return introductionValidator;
	}
	@Provides
	@Singleton
	IntroductionManager provideIntroductionManager(
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			IntroductionManagerImpl introductionManager,
			CleanupManager cleanupManager) {
		lifecycleManager.registerOpenDatabaseHook(introductionManager);
		contactManager.registerContactHook(introductionManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID,
				MAJOR_VERSION, introductionManager);
		conversationManager.registerConversationClient(introductionManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, introductionManager);
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				introductionManager);
		return introductionManager;
	}
	@Provides
	MessageParser provideMessageParser(MessageParserImpl messageParser) {
		return messageParser;
	}
	@Provides
	MessageEncoder provideMessageEncoder(MessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}
	@Provides
	SessionParser provideSessionParser(SessionParserImpl sessionParser) {
		return sessionParser;
	}
	@Provides
	SessionEncoder provideSessionEncoder(SessionEncoderImpl sessionEncoder) {
		return sessionEncoder;
	}
	@Provides
	IntroductionCrypto provideIntroductionCrypto(
			IntroductionCryptoImpl introductionCrypto) {
		return introductionCrypto;
	}
}