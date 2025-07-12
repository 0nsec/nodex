package org.nodex.feed;
import com.rometools.rome.feed.synd.SyndFeed;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.feed.Feed;
import javax.annotation.Nullable;
interface FeedFactory {
	Feed createFeed(@Nullable String url, SyndFeed sf);
	Feed updateFeed(Feed feed, SyndFeed sf, long lastEntryTime);
	Feed createFeed(BdfDictionary d) throws FormatException;
	BdfDictionary feedToBdfDictionary(Feed feed);
}