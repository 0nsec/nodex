package org.nodex.feed;
import org.nodex.api.feed.Feed;
import org.nodex.api.feed.RssProperties;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
@ThreadSafe
@NotNullByDefault
interface FeedMatcher {
	@Nullable
	Feed findMatchingFeed(RssProperties candidate, List<Feed> feeds);
}