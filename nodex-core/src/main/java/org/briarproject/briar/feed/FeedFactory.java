package org.briarproject.briar.feed;
import com.rometools.rome.feed.synd.SyndFeed;
import org.briarproject.bramble.api.FormatException;
import org.briarproject.bramble.api.data.BdfDictionary;
import org.briarproject.briar.api.feed.Feed;
import javax.annotation.Nullable;
interface FeedFactory {
	Feed createFeed(@Nullable String url, SyndFeed sf);
	Feed updateFeed(Feed feed, SyndFeed sf, long lastEntryTime);
	Feed createFeed(BdfDictionary d) throws FormatException;
	BdfDictionary feedToBdfDictionary(Feed feed);
}