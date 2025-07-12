package org.nodex.blog;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.test.TestDatabaseConfigModule;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogCommentHeader;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPost;
import org.nodex.api.blog.BlogPostHeader;
import org.nodex.test.BriarIntegrationTest;
import org.nodex.test.BriarIntegrationTestComponent;
import org.nodex.test.DaggerBriarIntegrationTestComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Collection;
import java.util.Iterator;
import static junit.framework.Assert.assertNotNull;
import static org.nodex.core.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.nodex.api.identity.AuthorInfo.Status.NONE;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.blog.MessageType.COMMENT;
import static org.nodex.api.blog.MessageType.POST;
import static org.nodex.api.blog.MessageType.WRAPPED_COMMENT;
import static org.nodex.api.blog.MessageType.WRAPPED_POST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
public class BlogManagerIntegrationTest
		extends BriarIntegrationTest<BriarIntegrationTestComponent> {
	private BlogManager blogManager0, blogManager1;
	private Blog blog0, blog1, rssBlog;
	private LocalAuthor rssAuthor;
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		String rssTitle = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		rssAuthor = c0.getAuthorFactory().createLocalAuthor(rssTitle);
		blogManager0 = c0.getBlogManager();
		blogManager1 = c1.getBlogManager();
		blog0 = blogFactory.createBlog(author0);
		blog1 = blogFactory.createBlog(author1);
		rssBlog = blogFactory.createFeedBlog(rssAuthor);
		db0.transaction(false, txn -> blogManager0.addBlog(txn, rssBlog));
	}
	@Override
	protected void createComponents() {
		BriarIntegrationTestComponent component =
				DaggerBriarIntegrationTestComponent.builder().build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(component);
		component.inject(this);
		c0 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t0Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c0);
		c1 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t1Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c1);
		c2 = DaggerBriarIntegrationTestComponent.builder()
				.testDatabaseConfigModule(new TestDatabaseConfigModule(t2Dir))
				.build();
		BriarIntegrationTestComponent.Helper.injectEagerSingletons(c2);
	}
	@Test
	public void testPersonalBlogInitialisation() throws Exception {
		Collection<Blog> blogs0 = blogManager0.getBlogs();
		assertEquals(4, blogs0.size());
		Iterator<Blog> i0 = blogs0.iterator();
		assertEquals(author0, i0.next().getAuthor());
		assertEquals(author1, i0.next().getAuthor());
		assertEquals(author2, i0.next().getAuthor());
		assertEquals(rssAuthor, i0.next().getAuthor());
		Collection<Blog> blogs1 = blogManager1.getBlogs();
		assertEquals(2, blogs1.size());
		Iterator<Blog> i1 = blogs1.iterator();
		assertEquals(author1, i1.next().getAuthor());
		assertEquals(author0, i1.next().getAuthor());
		assertEquals(blog0, blogManager0.getPersonalBlog(author0));
		assertEquals(blog0, blogManager1.getPersonalBlog(author0));
		assertEquals(blog1, blogManager0.getPersonalBlog(author1));
		assertEquals(blog1, blogManager1.getPersonalBlog(author1));
		assertEquals(blog0, blogManager0.getBlog(blog0.getId()));
		assertEquals(blog0, blogManager1.getBlog(blog0.getId()));
		assertEquals(blog1, blogManager0.getBlog(blog1.getId()));
		assertEquals(blog1, blogManager1.getBlog(blog1.getId()));
		assertEquals(rssBlog, blogManager0.getBlog(rssBlog.getId()));
		assertEquals(1, blogManager0.getBlogs(author0).size());
		assertEquals(1, blogManager1.getBlogs(author0).size());
		assertEquals(1, blogManager0.getBlogs(author1).size());
		assertEquals(1, blogManager1.getBlogs(author1).size());
		assertEquals(1, blogManager0.getBlogs(rssAuthor).size());
		assertEquals(0, blogManager1.getBlogs(rssAuthor).size());
	}
	@Test
	public void testBlogPost() throws Exception {
		String text = getRandomString(42);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog0.getId());
		assertEquals(0, headers0.size());
		BlogPost p = blogPostFactory.createBlogPost(blog0.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		headers0 = blogManager0.getPostHeaders(blog0.getId());
		assertEquals(1, headers0.size());
		assertEquals(text, blogManager0.getPostText(p.getMessage().getId()));
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog0.getId());
		assertEquals(0, headers1.size());
		sync0To1(1, true);
		headers1 = blogManager1.getPostHeaders(blog0.getId());
		assertEquals(1, headers1.size());
		assertEquals(POST, headers1.iterator().next().getType());
		assertEquals(text, blogManager1.getPostText(p.getMessage().getId()));
	}
	@Test
	public void testBlogPostInWrongBlog() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(blog1.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog1.getId());
		assertEquals(1, headers0.size());
		sync0To1(1, false);
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog1.getId());
		assertEquals(0, headers1.size());
	}
	@Test
	public void testCanRemoveContactsPersonalBlog() throws Exception {
		assertTrue(blogManager0.canBeRemoved(blog1));
		assertTrue(blogManager1.canBeRemoved(blog0));
		assertEquals(4, blogManager0.getBlogs().size());
		assertEquals(2, blogManager1.getBlogs().size());
		blogManager0.removeBlog(blog1);
		blogManager1.removeBlog(blog0);
		assertEquals(3, blogManager0.getBlogs().size());
		assertEquals(1, blogManager1.getBlogs().size());
	}
	@Test
	public void testBlogComment() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(blog0.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		sync0To1(1, true);
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog0.getId());
		assertEquals(1, headers1.size());
		assertEquals(POST, headers1.iterator().next().getType());
		String comment = "This is a comment on a blog post!";
		blogManager1
				.addLocalComment(author1, blog1.getId(), comment,
						headers1.iterator().next());
		sync1To0(2, true);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog1.getId());
		assertEquals(1, headers0.size());
		assertEquals(COMMENT, headers0.iterator().next().getType());
		BlogCommentHeader h = (BlogCommentHeader) headers0.iterator().next();
		assertEquals(author0, h.getParent().getAuthor());
		MessageId parentId = h.getParentId();
		assertNotNull(parentId);
		assertEquals(text, blogManager0.getPostText(parentId));
		headers1 = blogManager1.getPostHeaders(blog1.getId());
		assertEquals(1, headers1.size());
	}
	@Test
	public void testBlogCommentOnOwnPost() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(blog0.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog0.getId());
		assertEquals(1, headers0.size());
		BlogPostHeader header = headers0.iterator().next();
		String comment = "This is a comment on my own blog post!";
		blogManager0
				.addLocalComment(author0, blog0.getId(), comment, header);
		sync0To1(2, true);
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog0.getId());
		assertEquals(2, headers1.size());
		for (BlogPostHeader h : headers1) {
			if (h.getType() == POST) {
				assertEquals(text, blogManager1.getPostText(h.getId()));
			} else {
				assertEquals(comment, ((BlogCommentHeader) h).getComment());
			}
		}
	}
	@Test
	public void testCommentOnComment() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(blog0.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		sync0To1(1, true);
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog0.getId());
		assertEquals(1, headers1.size());
		assertEquals(POST, headers1.iterator().next().getType());
		blogManager1.addLocalComment(author1, blog1.getId(), null,
				headers1.iterator().next());
		sync1To0(2, true);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog1.getId());
		assertEquals(1, headers0.size());
		BlogPostHeader cHeader = headers0.iterator().next();
		assertEquals(COMMENT, cHeader.getType());
		String comment = "This is a comment on a reblogged post.";
		blogManager0
				.addLocalComment(author0, blog0.getId(), comment, cHeader);
		sync0To1(3, true);
		headers1 = blogManager1.getPostHeaders(blog0.getId());
		assertEquals(2, headers1.size());
		cHeader = null;
		for (BlogPostHeader h : headers1) {
			if (h.getType() == COMMENT) {
				cHeader = h;
			}
		}
		assertNotNull(cHeader);
		String comment2 = "This is a comment on a comment.";
		blogManager1.addLocalComment(author1, blog1.getId(), comment2, cHeader);
		sync1To0(4, true);
		headers0 =
				blogManager0.getPostHeaders(blog1.getId());
		assertEquals(2, headers0.size());
		boolean satisfied = false;
		for (BlogPostHeader h : headers0) {
			assertEquals(COMMENT, h.getType());
			BlogCommentHeader c = (BlogCommentHeader) h;
			if (c.getComment() != null && c.getComment().equals(comment2)) {
				assertEquals(author0, c.getParent().getAuthor());
				assertEquals(WRAPPED_COMMENT, c.getParent().getType());
				assertEquals(comment,
						((BlogCommentHeader) c.getParent()).getComment());
				assertEquals(WRAPPED_COMMENT,
						((BlogCommentHeader) c.getParent()).getParent()
								.getType());
				assertEquals(WRAPPED_POST,
						((BlogCommentHeader) ((BlogCommentHeader) c
								.getParent()).getParent()).getParent()
								.getType());
				satisfied = true;
			}
		}
		assertTrue(satisfied);
	}
	@Test
	public void testCommentOnOwnComment() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(blog0.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		sync0To1(1, true);
		Collection<BlogPostHeader> headers1 =
				blogManager1.getPostHeaders(blog0.getId());
		assertEquals(1, headers1.size());
		assertEquals(POST, headers1.iterator().next().getType());
		String comment = "This is a comment on a post.";
		blogManager1
				.addLocalComment(author1, blog1.getId(), comment,
						headers1.iterator().next());
		headers1 = blogManager1.getPostHeaders(blog1.getId());
		assertEquals(1, headers1.size());
		assertEquals(COMMENT, headers1.iterator().next().getType());
		BlogCommentHeader ch = (BlogCommentHeader) headers1.iterator().next();
		assertEquals(comment, ch.getComment());
		comment = "This is a comment on a post with a comment.";
		blogManager1.addLocalComment(author1, blog1.getId(), comment, ch);
		sync1To0(3, true);
		Collection<BlogPostHeader> headers0 =
				blogManager0.getPostHeaders(blog1.getId());
		assertEquals(2, headers0.size());
	}
	@Test
	public void testFeedPost() throws Exception {
		assertTrue(rssBlog.isRssFeed());
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(rssBlog.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		Collection<BlogPostHeader> headers =
				blogManager0.getPostHeaders(rssBlog.getId());
		assertEquals(1, headers.size());
		BlogPostHeader header = headers.iterator().next();
		assertEquals(POST, header.getType());
		assertEquals(NONE, header.getAuthorStatus());
		assertTrue(header.isRssFeed());
	}
	@Test
	public void testFeedReblog() throws Exception {
		String text = getRandomString(42);
		BlogPost p = blogPostFactory.createBlogPost(rssBlog.getId(),
				c0.getClock().currentTimeMillis(), null, author0, text);
		blogManager0.addLocalPost(p);
		Collection<BlogPostHeader> headers =
				blogManager0.getPostHeaders(rssBlog.getId());
		assertEquals(1, headers.size());
		BlogPostHeader header = headers.iterator().next();
		blogManager0.addLocalComment(author0, blog0.getId(), null, header);
		headers = blogManager0.getPostHeaders(blog0.getId());
		assertEquals(1, headers.size());
		BlogCommentHeader commentHeader =
				(BlogCommentHeader) headers.iterator().next();
		assertEquals(COMMENT, commentHeader.getType());
		assertTrue(commentHeader.getParent().isRssFeed());
		blogManager0
				.addLocalComment(author0, blog0.getId(), null, commentHeader);
		headers = blogManager0.getPostHeaders(blog0.getId());
		assertEquals(2, headers.size());
		for (BlogPostHeader h : headers) {
			assertTrue(h instanceof BlogCommentHeader);
			assertEquals(COMMENT, h.getType());
			assertTrue(((BlogCommentHeader) h).getRootPost().isRssFeed());
		}
	}
	@Test
	public void testRemoveContacts() throws Exception {
		assertTrue(blogManager0.getBlogs().contains(blog1));
		contactManager0.removeContact(contactId1From0);
		assertFalse(blogManager0.getBlogs().contains(blog1));
		assertTrue(blogManager1.getBlogs().contains(blog0));
		contactManager1.removeContact(contactId0From1);
		assertFalse(blogManager1.getBlogs().contains(blog0));
	}
}