package org.nodex.blog;
import org.nodex.core.api.FeatureFlags;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.data.MetadataEncoder;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupFactory;
import org.nodex.core.api.sync.MessageFactory;
import org.nodex.core.api.sync.validation.ValidationManager;
import org.nodex.core.api.system.Clock;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPostFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
import static org.nodex.api.blog.BlogManager.MAJOR_VERSION;
@Module
public class BlogModule {
	public static class EagerSingletons {
		@Inject
		BlogPostValidator blogPostValidator;
		@Inject
		BlogManager blogManager;
	}
	@Provides
	@Singleton
	BlogManager provideBlogManager(BlogManagerImpl blogManager,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnableBlogsInCore()) {
			return blogManager;
		}
		lifecycleManager.registerOpenDatabaseHook(blogManager);
		contactManager.registerContactHook(blogManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				blogManager);
		return blogManager;
	}
	@Provides
	BlogPostFactory provideBlogPostFactory(
			BlogPostFactoryImpl blogPostFactory) {
		return blogPostFactory;
	}
	@Provides
	BlogFactory provideBlogFactory(BlogFactoryImpl blogFactory) {
		return blogFactory;
	}
	@Provides
	@Singleton
	BlogPostValidator provideBlogPostValidator(
			ValidationManager validationManager, GroupFactory groupFactory,
			MessageFactory messageFactory, BlogFactory blogFactory,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, FeatureFlags featureFlags) {
		BlogPostValidator validator = new BlogPostValidator(groupFactory,
				messageFactory, blogFactory, clientHelper, metadataEncoder,
				clock);
		if (featureFlags.shouldEnableBlogsInCore()) {
			validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
					validator);
		}
		return validator;
	}
}