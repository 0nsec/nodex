package org.nodex.avatar;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.data.BdfReaderFactory;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.validation.ValidationManager;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.avatar.AvatarManager;
import org.nodex.api.avatar.AvatarMessageEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static org.nodex.api.avatar.AvatarManager.CLIENT_ID;
import static org.nodex.api.avatar.AvatarManager.MAJOR_VERSION;
import static org.nodex.api.avatar.AvatarManager.MINOR_VERSION;
@Module
public class AvatarModule {
	public static class EagerSingletons {
		@Inject
		AvatarValidator avatarValidator;
		@Inject
		AvatarManager avatarManager;
	}
	@Provides
	@Singleton
	AvatarValidator provideAvatarValidator(ValidationManager validationManager,
			BdfReaderFactory bdfReaderFactory, MetadataEncoder metadataEncoder,
			Clock clock) {
		AvatarValidator avatarValidator =
				new AvatarValidator(bdfReaderFactory, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID.toString(), MAJOR_VERSION,
				avatarValidator);
		return avatarValidator;
	}
	@Provides
	@Singleton
	AvatarMessageEncoder provideMessageEncoder(
			AvatarMessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}
	@Provides
	@Singleton
	AvatarManager provideAvatarManager(
			LifecycleManager lifecycleManager,
			ContactManager contactManager,
			ValidationManager validationManager,
			ClientVersioningManager clientVersioningManager,
			AvatarManagerImpl avatarManager) {
		lifecycleManager.registerOpenDatabaseHook(avatarManager);
		contactManager.registerContactHook(avatarManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID.getString(),
				MAJOR_VERSION, avatarManager);
		clientVersioningManager.registerClient(CLIENT_ID.getString(),
				MAJOR_VERSION, MINOR_VERSION, avatarManager);
		return avatarManager;
	}
}
