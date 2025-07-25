package org.nodex.sharing;
import org.nodex.api.FeatureFlags;
import org.nodex.api.cleanup.CleanupManager;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.contact.ContactManager;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.sync.validation.ValidationManager;
import org.nodex.api.system.Clock;
import org.nodex.api.versioning.ClientVersioningManager;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogInvitationResponse;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogSharingManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumFactory;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumSharingManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class SharingModule {
	public static class EagerSingletons {
		@Inject
		BlogSharingValidator blogSharingValidator;
		@Inject
		ForumSharingValidator forumSharingValidator;
		@Inject
		ForumSharingManager forumSharingManager;
		@Inject
		BlogSharingManager blogSharingManager;
	}
	@Provides
	MessageEncoder provideMessageEncoder(MessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}
	@Provides
	SessionEncoder provideSessionEncoder(SessionEncoderImpl sessionEncoder) {
		return sessionEncoder;
	}
	@Provides
	SessionParser provideSessionParser(SessionParserImpl sessionParser) {
		return sessionParser;
	}
	@Provides
	@Singleton
	BlogSharingValidator provideBlogSharingValidator(
			ValidationManager validationManager, MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, BlogFactory blogFactory, FeatureFlags featureFlags) {
		BlogSharingValidator validator = new BlogSharingValidator(
				messageEncoder, clientHelper, metadataEncoder, clock,
				blogFactory);
		if (featureFlags.shouldEnableBlogsInCore()) {
			validationManager.registerMessageValidator(
					BlogSharingManager.CLIENT_ID,
					BlogSharingManager.MAJOR_VERSION, validator);
		}
		return validator;
	}
	@Provides
	@Singleton
	BlogSharingManager provideBlogSharingManager(
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager,
			ConversationManager conversationManager, BlogManager blogManager,
			ClientVersioningManager clientVersioningManager,
			BlogSharingManagerImpl blogSharingManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnableBlogsInCore()) {
			return blogSharingManager;
		}
		lifecycleManager.registerOpenDatabaseHook(blogSharingManager);
		contactManager.registerContactHook(blogSharingManager);
		validationManager.registerIncomingMessageHook(
				BlogSharingManager.CLIENT_ID, BlogSharingManager.MAJOR_VERSION,
				blogSharingManager);
		conversationManager.registerConversationClient(blogSharingManager);
		blogManager.registerRemoveBlogHook(blogSharingManager);
		clientVersioningManager.registerClient(BlogSharingManager.CLIENT_ID,
				BlogSharingManager.MAJOR_VERSION,
				BlogSharingManager.MINOR_VERSION, blogSharingManager);
		clientVersioningManager.registerClient(BlogManager.CLIENT_ID,
				BlogManager.MAJOR_VERSION, BlogManager.MINOR_VERSION,
				blogSharingManager.getShareableClientVersioningHook());
		cleanupManager.registerCleanupHook(BlogSharingManager.CLIENT_ID,
				BlogSharingManager.MAJOR_VERSION,
				blogSharingManager);
		return blogSharingManager;
	}
	@Provides
	MessageParser<Blog> provideBlogMessageParser(
			BlogMessageParserImpl blogMessageParser) {
		return blogMessageParser;
	}
	@Provides
	ProtocolEngine<Blog> provideBlogProtocolEngine(
			BlogProtocolEngineImpl blogProtocolEngine) {
		return blogProtocolEngine;
	}
	@Provides
	InvitationFactory<Blog, BlogInvitationResponse> provideBlogInvitationFactory(
			BlogInvitationFactoryImpl blogInvitationFactory) {
		return blogInvitationFactory;
	}
	@Provides
	@Singleton
	ForumSharingValidator provideForumSharingValidator(
			ValidationManager validationManager, MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ForumFactory forumFactory, FeatureFlags featureFlags) {
		ForumSharingValidator validator = new ForumSharingValidator(
				messageEncoder, clientHelper, metadataEncoder, clock,
				forumFactory);
		if (featureFlags.shouldEnableForumsInCore()) {
			validationManager.registerMessageValidator(
					ForumSharingManager.CLIENT_ID,
					ForumSharingManager.MAJOR_VERSION, validator);
		}
		return validator;
	}
	@Provides
	@Singleton
	ForumSharingManager provideForumSharingManager(
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager,
			ConversationManager conversationManager, ForumManager forumManager,
			ClientVersioningManager clientVersioningManager,
			ForumSharingManagerImpl forumSharingManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnableForumsInCore()) {
			return forumSharingManager;
		}
		lifecycleManager.registerOpenDatabaseHook(forumSharingManager);
		contactManager.registerContactHook(forumSharingManager);
		validationManager.registerIncomingMessageHook(
				ForumSharingManager.CLIENT_ID,
				ForumSharingManager.MAJOR_VERSION, forumSharingManager);
		conversationManager.registerConversationClient(forumSharingManager);
		forumManager.registerRemoveForumHook(forumSharingManager);
		clientVersioningManager.registerClient(ForumSharingManager.CLIENT_ID,
				ForumSharingManager.MAJOR_VERSION,
				ForumSharingManager.MINOR_VERSION, forumSharingManager);
		clientVersioningManager.registerClient(ForumManager.CLIENT_ID,
				ForumManager.MAJOR_VERSION, ForumManager.MINOR_VERSION,
				forumSharingManager.getShareableClientVersioningHook());
		cleanupManager.registerCleanupHook(ForumSharingManager.CLIENT_ID,
				ForumSharingManager.MAJOR_VERSION,
				forumSharingManager);
		return forumSharingManager;
	}
	@Provides
	MessageParser<Forum> provideForumMessageParser(
			ForumMessageParserImpl forumMessageParser) {
		return forumMessageParser;
	}
	@Provides
	ProtocolEngine<Forum> provideForumProtocolEngine(
			ForumProtocolEngineImpl forumProtocolEngine) {
		return forumProtocolEngine;
	}
	@Provides
	InvitationFactory<Forum, ForumInvitationResponse> provideForumInvitationFactory(
			ForumInvitationFactoryImpl forumInvitationFactory) {
		return forumInvitationFactory;
	}
}
