package org.nodex.feed;
import org.nodex.api.FeatureFlags;
import org.nodex.api.event.EventBus;
import org.nodex.api.lifecycle.LifecycleManager;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.feed.FeedManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
@Module
public class FeedModule {
	public static class EagerSingletons {
		@Inject
		FeedManager feedManager;
	}
	@Provides
	@Singleton
	FeedManager provideFeedManager(FeedManagerImpl feedManager,
			LifecycleManager lifecycleManager, EventBus eventBus,
			BlogManager blogManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnableBlogsInCore()) {
			return feedManager;
		}
		lifecycleManager.registerOpenDatabaseHook(feedManager);
		eventBus.addListener(feedManager);
		blogManager.registerRemoveBlogHook(feedManager);
		return feedManager;
	}
	@Provides
	FeedFactory provideFeedFactory(FeedFactoryImpl feedFactory) {
		return feedFactory;
	}
	@Provides
	FeedMatcher provideFeedMatcher(FeedMatcherImpl feedMatcher) {
		return feedMatcher;
	}
}
