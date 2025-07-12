package org.briarproject.briar.feed;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.bramble.api.sync.ClientId;
import org.briarproject.bramble.test.BrambleTestCase;
import org.briarproject.briar.api.blog.Blog;
import org.briarproject.briar.api.feed.Feed;
import org.briarproject.briar.api.feed.RssProperties;
import org.junit.Test;
import java.util.Random;
import static java.util.Arrays.asList;
import static org.briarproject.bramble.test.TestUtils.getClientId;
import static org.briarproject.bramble.test.TestUtils.getGroup;
import static org.briarproject.bramble.test.TestUtils.getLocalAuthor;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
public class FeedMatcherImplTest extends BrambleTestCase {
	private static final String URL = "url";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String AUTHOR = "author";
	private static final String LINK = "link";
	private static final String URI = "uri";
	private final Random random = new Random();
	private final ClientId clientId = getClientId();
	private final LocalAuthor localAuthor = getLocalAuthor();
	private final FeedMatcher matcher = new FeedMatcherImpl();
	@Test
	public void testFeedWithMatchingUrlIsChosen() {
		RssProperties candidate = new RssProperties(URL,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI);
		Feed feed1 = createFeed(new RssProperties(nope(),
				TITLE, DESCRIPTION, AUTHOR, LINK, URI));
		Feed feed2 = createFeed(new RssProperties(URL,
				nope(), nope(), nope(), nope(), nope()));
		Feed match = matcher.findMatchingFeed(candidate, asList(feed1, feed2));
		assertNotNull(match);
		assertSame(feed2, match);
	}
	@Test
	public void testNullUrlIsNotMatched() {
		RssProperties candidate = new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI);
		Feed feed1 = createFeed(new RssProperties(URL,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI));
		Feed feed2 = createFeed(new RssProperties(null,
				nope(), nope(), nope(), nope(), nope()));
		Feed match = matcher.findMatchingFeed(candidate, asList(feed1, feed2));
		assertNotNull(match);
		assertSame(feed1, match);
	}
	@Test
	public void testDoesNotMatchOneRssField() {
		testDoesNotMatchRssFields(TITLE, nope(), nope(), nope(), nope());
		testDoesNotMatchRssFields(nope(), DESCRIPTION, nope(), nope(), nope());
		testDoesNotMatchRssFields(nope(), nope(), AUTHOR, nope(), nope());
		testDoesNotMatchRssFields(nope(), nope(), nope(), LINK, nope());
		testDoesNotMatchRssFields(nope(), nope(), nope(), nope(), URL);
	}
	private void testDoesNotMatchRssFields(String title, String description,
			String author, String link, String uri) {
		RssProperties candidate = new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, LINK, URL);
		Feed feed1 = createFeed(new RssProperties(null,
				nope(), nope(), nope(), nope(), nope()));
		Feed feed2 = createFeed(new RssProperties(null,
				title, description, author, link, uri));
		Feed match = matcher.findMatchingFeed(candidate, asList(feed1, feed2));
		assertNull(match);
	}
	@Test
	public void testMatchesTwoRssFields() {
		testMatchesRssFields(TITLE, DESCRIPTION, nope(), nope(), nope());
		testMatchesRssFields(nope(), DESCRIPTION, AUTHOR, nope(), nope());
		testMatchesRssFields(nope(), nope(), AUTHOR, LINK, nope());
		testMatchesRssFields(nope(), nope(), nope(), LINK, URI);
	}
	private void testMatchesRssFields(String title, String description,
			String author, String link, String uri) {
		RssProperties candidate = new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI);
		Feed feed1 = createFeed(new RssProperties(null,
				nope(), nope(), nope(), nope(), nope()));
		Feed feed2 = createFeed(new RssProperties(null,
				title, description, author, link, uri));
		Feed feed3 = createFeed(new RssProperties(null,
				TITLE, nope(), nope(), nope(), nope()));
		FeedMatcher matcher = new FeedMatcherImpl();
		Feed match = matcher.findMatchingFeed(candidate,
				asList(feed1, feed2, feed3));
		assertSame(feed2, match);
	}
	@Test
	public void testFeedWithMostMatchingRssFieldsIsChosen() {
		RssProperties candidate = new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI);
		Feed feed1 = createFeed(new RssProperties(null,
				nope(), nope(), nope(), nope(), nope()));
		Feed feed2 = createFeed(new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, nope(), nope()));
		Feed feed3 = createFeed(new RssProperties(null,
				TITLE, DESCRIPTION, nope(), nope(), nope()));
		Feed match = matcher.findMatchingFeed(candidate,
				asList(feed1, feed2, feed3));
		assertSame(feed2, match);
	}
	@Test
	public void testNullRssFieldsAreNotMatched() {
		RssProperties candidate = new RssProperties(null,
				null, null, null, null, null);
		Feed feed1 = createFeed(new RssProperties(null,
				TITLE, DESCRIPTION, AUTHOR, LINK, URI));
		Feed feed2 = createFeed(new RssProperties(URL,
				null, null, null, null, null));
		Feed match = matcher.findMatchingFeed(candidate, asList(feed1, feed2));
		assertNull(match);
	}
	private String nope() {
		return random.nextBoolean() ? null : "x";
	}
	private Feed createFeed(RssProperties properties) {
		Blog blog = new Blog(getGroup(clientId, 123), localAuthor, true);
		return new Feed(blog, localAuthor, properties, 0, 0, 0);
	}
}