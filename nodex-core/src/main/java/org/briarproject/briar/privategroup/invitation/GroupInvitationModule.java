package org.nodex.privategroup.invitation;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.cleanup.CleanupManager;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.data.MetadataEncoder;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.validation.ValidationManager;
import org.nodex.core.api.system.Clock;
import org.nodex.core.api.versioning.ClientVersioningManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.invitation.GroupInvitationFactory;
import org.nodex.api.privategroup.invitation.GroupInvitationManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.MAJOR_VERSION;
import static org.nodex.api.privategroup.invitation.GroupInvitationManager.MINOR_VERSION;
@Module
public class GroupInvitationModule {
	public static class EagerSingletons {
		@Inject
		GroupInvitationValidator groupInvitationValidator;
		@Inject
		GroupInvitationManager groupInvitationManager;
	}
	@Provides
	@Singleton
	GroupInvitationManager provideGroupInvitationManager(
			GroupInvitationManagerImpl groupInvitationManager,
			LifecycleManager lifecycleManager,
			ValidationManager validationManager, ContactManager contactManager,
			PrivateGroupManager privateGroupManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnablePrivateGroupsInCore()) {
			return groupInvitationManager;
		}
		lifecycleManager.registerOpenDatabaseHook(groupInvitationManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				groupInvitationManager);
		contactManager.registerContactHook(groupInvitationManager);
		privateGroupManager.registerPrivateGroupHook(groupInvitationManager);
		conversationManager.registerConversationClient(groupInvitationManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, groupInvitationManager);
		clientVersioningManager.registerClient(PrivateGroupManager.CLIENT_ID,
				PrivateGroupManager.MAJOR_VERSION,
				PrivateGroupManager.MINOR_VERSION,
				groupInvitationManager.getPrivateGroupClientVersioningHook());
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				groupInvitationManager);
		return groupInvitationManager;
	}
	@Provides
	@Singleton
	GroupInvitationValidator provideGroupInvitationValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, PrivateGroupFactory privateGroupFactory,
			MessageEncoder messageEncoder,
			ValidationManager validationManager,
			FeatureFlags featureFlags) {
		GroupInvitationValidator validator = new GroupInvitationValidator(
				clientHelper, metadataEncoder, clock, privateGroupFactory,
				messageEncoder);
		if (featureFlags.shouldEnablePrivateGroupsInCore()) {
			validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
					validator);
		}
		return validator;
	}
	@Provides
	GroupInvitationFactory provideGroupInvitationFactory(
			GroupInvitationFactoryImpl groupInvitationFactory) {
		return groupInvitationFactory;
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
	ProtocolEngineFactory provideProtocolEngineFactory(
			ProtocolEngineFactoryImpl protocolEngineFactory) {
		return protocolEngineFactory;
	}
}