package org.nodex.api.feed;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.ClientId;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
@NotNullByDefault
public interface FeedManager {
	ClientId CLIENT_ID = new ClientId("org.nodex.feed");
	int MAJOR_VERSION = 0;
	Feed addFeed(String url) throws DbException, IOException;
	Feed addFeed(InputStream in) throws DbException, IOException;
	void removeFeed(Feed feed) throws DbException;
	List<Feed> getFeeds() throws DbException;
	List<Feed> getFeeds(Transaction txn) throws DbException;
}