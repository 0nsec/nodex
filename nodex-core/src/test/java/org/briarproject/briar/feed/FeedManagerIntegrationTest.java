package org.nodex.feed;
import org.nodex.core.api.identity.Identity;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.test.BrambleTestCase;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPostHeader;
import org.nodex.api.feed.Feed;
import org.nodex.api.feed.FeedManager;
import org.nodex.nullsafety.NullSafety;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import javax.annotation.Nullable;
import static org.nodex.core.test.TestUtils.deleteTestDirectory;
import static org.nodex.core.test.TestUtils.getSecretKey;
import static org.nodex.core.test.TestUtils.getTestDirectory;
import static org.nodex.nullsafety.NullSafety.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
public class FeedManagerIntegrationTest extends BrambleTestCase {
	private static final String FEED_PATH =
			"src/test/resources/nodex.org_news_index.xml";
	private static final String FEED_URL =
			"https:
	private static final String FEED_TITLE = "News - Briar";
	private LifecycleManager lifecycleManager;
	private FeedManager feedManager;
	private BlogManager blogManager;
	private final File testDir = getTestDirectory();
	private final File testFile = new File(testDir, "feedTest");
	@Before
	public void setUp() throws Exception {
		assertTrue(testDir.mkdirs());
		FeedManagerIntegrationTestComponent component =
				DaggerFeedManagerIntegrationTestComponent.builder()
						.testDatabaseConfigModule(
								new TestDatabaseConfigModule(testFile)).build();
		FeedManagerIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
		IdentityManager identityManager = component.getIdentityManager();
		Identity identity = identityManager.createIdentity("feedTest");
		identityManager.registerIdentity(identity);
		lifecycleManager = component.getLifecycleManager();
		lifecycleManager.startServices(getSecretKey());
		lifecycleManager.waitForStartup();
		feedManager = component.getFeedManager();
		blogManager = component.getBlogManager();
	}
	@Test
	public void testFeedImportAndRemovalFromUrl() throws Exception {
		testFeedImportAndRemoval(FEED_URL, null);
	}
	@Test
	public void testFeedImportAndRemovalFromFile() throws Exception {
		testFeedImportAndRemoval(null, FEED_PATH);
	}
	private void testFeedImportAndRemoval(@Nullable String url,
			@Nullable String path) throws Exception {
		Collection<Blog> blogs = blogManager.getBlogs();
		assertEquals(1, blogs.size());
		Blog personalBlog = blogs.iterator().next();
		if (url == null) {
			feedManager.addFeed(new FileInputStream(requireNonNull(path)));
		} else {
			feedManager.addFeed(url);
		}
		blogs = blogManager.getBlogs();
		assertEquals(2, blogs.size());
		Blog feedBlog = null;
		for (Blog blog : blogs) {
			if (!blog.equals(personalBlog)) feedBlog = blog;
		}
		assertNotNull(feedBlog);
		Collection<Feed> feeds = feedManager.getFeeds();
		assertEquals(1, feeds.size());
		Feed feed = feeds.iterator().next();
		assertTrue(feed.getLastEntryTime() > 0);
		assertTrue(feed.getAdded() > 0);
		assertTrue(feed.getUpdated() > 0);
		assertTrue(NullSafety.equals(url, feed.getProperties().getUrl()));
		assertEquals(feedBlog, feed.getBlog());
		assertEquals(FEED_TITLE, feed.getTitle());
		assertEquals(feed.getTitle(), feed.getBlog().getName());
		assertEquals(feed.getTitle(), feed.getLocalAuthor().getName());
		Collection<BlogPostHeader> headers =
				blogManager.getPostHeaders(feedBlog.getId());
		assertFalse(headers.isEmpty());
		for (BlogPostHeader header : headers) {
			assertTrue(header.isRssFeed());
		}
		blogManager.removeBlog(feedBlog);
		blogs = blogManager.getBlogs();
		assertEquals(1, blogs.size());
		assertEquals(personalBlog, blogs.iterator().next());
		assertEquals(0, feedManager.getFeeds().size());
	}
	@After
	public void tearDown() throws Exception {
		lifecycleManager.stopServices();
		lifecycleManager.waitForShutdown();
		deleteTestDirectory(testDir);
	}
}