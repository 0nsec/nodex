package org.nodex.feed;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.Group;
import org.nodex.api.system.Clock;
import org.nodex.core.test.BrambleMockTestCase;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.feed.Feed;
import org.nodex.api.feed.RssProperties;
import org.jmock.Expectations;
import org.junit.Test;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getLocalAuthor;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
import static org.nodex.api.blog.BlogManager.MAJOR_VERSION;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_ADDED;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_AUTHOR;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_DESC;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_LAST_ENTRY;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_PRIVATE_KEY;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_RSS_AUTHOR;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_RSS_LINK;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_RSS_TITLE;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_RSS_URI;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_UPDATED;
import static org.nodex.api.feed.FeedConstants.KEY_FEED_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
public class FeedFactoryImplTest extends BrambleMockTestCase {
	private final AuthorFactory authorFactory =
			context.mock(AuthorFactory.class);
	private final BlogFactory blogFactory = context.mock(BlogFactory.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final Clock clock = context.mock(Clock.class);
	private final LocalAuthor localAuthor = getLocalAuthor();
	private final Group blogGroup = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final Blog blog = new Blog(blogGroup, localAuthor, true);
	private final BdfList authorList = BdfList.of("foo");
	private final long added = 123, updated = 234, lastEntryTime = 345;
	private final String url = getRandomString(123);
	private final String description = getRandomString(123);
	private final String rssAuthor = getRandomString(123);
	private final String title = getRandomString(123);
	private final String link = getRandomString(123);
	private final String uri = getRandomString(123);
	private final FeedFactoryImpl feedFactory = new FeedFactoryImpl(
			authorFactory, blogFactory, clientHelper, clock);
	@Test
	public void testSerialiseAndDeserialiseWithoutOptionalFields()
			throws Exception {
		RssProperties propertiesBefore = new RssProperties(null, null, null,
				null, null, null);
		Feed before = new Feed(blog, localAuthor, propertiesBefore, added,
				updated, lastEntryTime);
		context.checking(new Expectations() {{
			oneOf(clientHelper).toList(localAuthor);
			will(returnValue(authorList));
		}});
		BdfDictionary dict = feedFactory.feedToBdfDictionary(before);
		BdfDictionary expectedDict = BdfDictionary.of(
				BdfEntry.of(KEY_FEED_AUTHOR, authorList),
				BdfEntry.of(KEY_FEED_PRIVATE_KEY, localAuthor.getPrivateKey()),
				BdfEntry.of(KEY_FEED_ADDED, added),
				BdfEntry.of(KEY_FEED_UPDATED, updated),
				BdfEntry.of(KEY_FEED_LAST_ENTRY, lastEntryTime)
		);
		assertEquals(expectedDict, dict);
		context.checking(new Expectations() {{
			oneOf(clientHelper).parseAndValidateAuthor(authorList);
			will(returnValue(localAuthor));
			oneOf(blogFactory).createFeedBlog(localAuthor);
			will(returnValue(blog));
		}});
		Feed after = feedFactory.createFeed(dict);
		RssProperties afterProperties = after.getProperties();
		assertNull(afterProperties.getUrl());
		assertNull(afterProperties.getTitle());
		assertNull(afterProperties.getDescription());
		assertNull(afterProperties.getAuthor());
		assertNull(afterProperties.getLink());
		assertNull(afterProperties.getUri());
		assertEquals(added, after.getAdded());
		assertEquals(updated, after.getUpdated());
		assertEquals(lastEntryTime, after.getLastEntryTime());
	}
	@Test
	public void testSerialiseAndDeserialiseWithOptionalFields()
			throws Exception {
		RssProperties propertiesBefore = new RssProperties(url, title,
				description, rssAuthor, link, uri);
		Feed before = new Feed(blog, localAuthor, propertiesBefore, added,
				updated, lastEntryTime);
		context.checking(new Expectations() {{
			oneOf(clientHelper).toList(localAuthor);
			will(returnValue(authorList));
		}});
		BdfDictionary dict = feedFactory.feedToBdfDictionary(before);
		BdfDictionary expectedDict = BdfDictionary.of(
				BdfEntry.of(KEY_FEED_AUTHOR, authorList),
				BdfEntry.of(KEY_FEED_PRIVATE_KEY, localAuthor.getPrivateKey()),
				BdfEntry.of(KEY_FEED_ADDED, added),
				BdfEntry.of(KEY_FEED_UPDATED, updated),
				BdfEntry.of(KEY_FEED_LAST_ENTRY, lastEntryTime),
				BdfEntry.of(KEY_FEED_URL, url),
				BdfEntry.of(KEY_FEED_RSS_TITLE, title),
				BdfEntry.of(KEY_FEED_DESC, description),
				BdfEntry.of(KEY_FEED_RSS_AUTHOR, rssAuthor),
				BdfEntry.of(KEY_FEED_RSS_LINK, link),
				BdfEntry.of(KEY_FEED_RSS_URI, uri)
		);
		assertEquals(expectedDict, dict);
		context.checking(new Expectations() {{
			oneOf(clientHelper).parseAndValidateAuthor(authorList);
			will(returnValue(localAuthor));
			oneOf(blogFactory).createFeedBlog(localAuthor);
			will(returnValue(blog));
		}});
		Feed after = feedFactory.createFeed(dict);
		RssProperties afterProperties = after.getProperties();
		assertEquals(url, afterProperties.getUrl());
		assertEquals(title, afterProperties.getTitle());
		assertEquals(description, afterProperties.getDescription());
		assertEquals(rssAuthor, afterProperties.getAuthor());
		assertEquals(link, afterProperties.getLink());
		assertEquals(uri, afterProperties.getUri());
		assertEquals(added, after.getAdded());
		assertEquals(updated, after.getUpdated());
		assertEquals(lastEntryTime, after.getLastEntryTime());
	}
}
