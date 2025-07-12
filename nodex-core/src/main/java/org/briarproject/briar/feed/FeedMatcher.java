package org.briarproject.briar.feed;
import org.briarproject.briar.api.feed.Feed;
import org.briarproject.briar.api.feed.RssProperties;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
@ThreadSafe
@NotNullByDefault
interface FeedMatcher {
	@Nullable
	Feed findMatchingFeed(RssProperties candidate, List<Feed> feeds);
}