package org.nodex.blog;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.core.system.SystemClock;
import org.nodex.core.test.BrambleMockTestCase;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.jmock.Expectations;
import org.junit.Test;
import java.io.IOException;
import java.security.GeneralSecurityException;
import static org.nodex.core.test.TestUtils.getAuthor;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getMessage;
import static org.nodex.core.test.TestUtils.getRandomBytes;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.blog.BlogConstants.KEY_AUTHOR;
import static org.nodex.api.blog.BlogConstants.KEY_COMMENT;
import static org.nodex.api.blog.BlogConstants.KEY_ORIGINAL_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_ORIGINAL_PARENT_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_PARENT_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_READ;
import static org.nodex.api.blog.BlogConstants.KEY_RSS_FEED;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
import static org.nodex.api.blog.BlogManager.MAJOR_VERSION;
import static org.nodex.api.blog.BlogPostFactory.SIGNING_LABEL_COMMENT;
import static org.nodex.api.blog.BlogPostFactory.SIGNING_LABEL_POST;
import static org.nodex.api.blog.MessageType.COMMENT;
import static org.nodex.api.blog.MessageType.POST;
import static org.nodex.api.blog.MessageType.WRAPPED_COMMENT;
import static org.nodex.api.blog.MessageType.WRAPPED_POST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
public class BlogPostValidatorTest extends BrambleMockTestCase {
	private final Blog blog, rssBlog;
	private final BdfList authorList;
	private final byte[] descriptor;
	private final Group group;
	private final Message message;
	private final BlogPostValidator validator;
	private final GroupFactory groupFactory = context.mock(GroupFactory.class);
	private final MessageFactory messageFactory =
			context.mock(MessageFactory.class);
	private final BlogFactory blogFactory = context.mock(BlogFactory.class);
	private final ClientHelper clientHelper = context.mock(ClientHelper.class);
	private final Author author;
	private final String text = getRandomString(42);
	public BlogPostValidatorTest() {
		group = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
		descriptor = group.getDescriptor();
		author = getAuthor();
		authorList = BdfList.of(
				author.getFormatVersion(),
				author.getName(),
				author.getPublicKey()
		);
		blog = new Blog(group, author, false);
		rssBlog = new Blog(group, author, true);
		message = getMessage(group.getId());
		MetadataEncoder metadataEncoder = context.mock(MetadataEncoder.class);
		Clock clock = new SystemClock();
		validator = new BlogPostValidator(groupFactory, messageFactory,
				blogFactory, clientHelper, metadataEncoder, clock);
		context.assertIsSatisfied();
	}
	@Test
	public void testValidateProperBlogPost()
			throws IOException, GeneralSecurityException {
		testValidateProperBlogPost(blog, false);
	}
	@Test
	public void testValidateProperRssBlogPost()
			throws IOException, GeneralSecurityException {
		testValidateProperBlogPost(rssBlog, true);
	}
	private void testValidateProperBlogPost(Blog b, boolean rssFeed)
			throws IOException, GeneralSecurityException {
		byte[] sigBytes = getRandomBytes(42);
		BdfList m = BdfList.of(POST.getInt(), text, sigBytes);
		BdfList signed = BdfList.of(b.getId(), message.getTimestamp(), text);
		expectCrypto(b, SIGNING_LABEL_POST, signed, sigBytes);
		BdfDictionary result =
				validator.validateMessage(message, group, m).getDictionary();
		assertEquals(authorList, result.getList(KEY_AUTHOR));
		assertFalse(result.getBoolean(KEY_READ));
		assertEquals(rssFeed, result.getBoolean(KEY_RSS_FEED));
		context.assertIsSatisfied();
	}
	@Test(expected = FormatException.class)
	public void testValidateBlogPostWithoutAttachments() throws IOException {
		BdfList content = BdfList.of(null, null, text);
		BdfList m = BdfList.of(POST.getInt(), content, null);
		validator.validateMessage(message, group, m);
	}
	@Test(expected = FormatException.class)
	public void testValidateBlogPostWithoutSignature() throws IOException {
		BdfList content = BdfList.of(null, null, text, null);
		BdfList m = BdfList.of(POST.getInt(), content, null);
		validator.validateMessage(message, group, m);
	}
	@Test
	public void testValidateProperBlogComment()
			throws IOException, GeneralSecurityException {
		String comment = "This is a blog comment";
		MessageId pOriginalId = new MessageId(getRandomId());
		MessageId currentId = new MessageId(getRandomId());
		byte[] sigBytes = getRandomBytes(42);
		BdfList m = BdfList.of(COMMENT.getInt(), comment, pOriginalId,
				currentId, sigBytes);
		BdfList signed = BdfList.of(blog.getId(), message.getTimestamp(),
				comment, pOriginalId, currentId);
		expectCrypto(blog, SIGNING_LABEL_COMMENT, signed, sigBytes);
		BdfDictionary result =
				validator.validateMessage(message, group, m).getDictionary();
		assertEquals(comment, result.getString(KEY_COMMENT));
		assertEquals(authorList, result.getList(KEY_AUTHOR));
		assertEquals(pOriginalId.getBytes(),
				result.getRaw(KEY_ORIGINAL_PARENT_MSG_ID));
		assertEquals(currentId.getBytes(), result.getRaw(KEY_PARENT_MSG_ID));
		assertFalse(result.getBoolean(KEY_READ));
		context.assertIsSatisfied();
	}
	@Test
	public void testValidateProperEmptyBlogComment()
			throws IOException, GeneralSecurityException {
		MessageId originalId = new MessageId(getRandomId());
		MessageId currentId = new MessageId(getRandomId());
		byte[] sigBytes = getRandomBytes(42);
		BdfList m = BdfList.of(COMMENT.getInt(), null, originalId, currentId,
				sigBytes);
		BdfList signed = BdfList.of(blog.getId(), message.getTimestamp(), null,
				originalId, currentId);
		expectCrypto(blog, SIGNING_LABEL_COMMENT, signed, sigBytes);
		BdfDictionary result =
				validator.validateMessage(message, group, m).getDictionary();
		assertFalse(result.containsKey(KEY_COMMENT));
		context.assertIsSatisfied();
	}
	@Test
	public void testValidateProperWrappedPost()
			throws IOException, GeneralSecurityException {
		testValidateProperWrappedPost(blog, false);
	}
	@Test
	public void testValidateProperWrappedRssPost()
			throws IOException, GeneralSecurityException {
		testValidateProperWrappedPost(rssBlog, true);
	}
	private void testValidateProperWrappedPost(Blog b, boolean rssFeed)
			throws IOException, GeneralSecurityException {
		byte[] sigBytes = getRandomBytes(42);
		BdfList m = BdfList.of(WRAPPED_POST.getInt(), descriptor,
				message.getTimestamp(), text, sigBytes);
		BdfList signed = BdfList.of(b.getId(), message.getTimestamp(), text);
		expectCrypto(b, SIGNING_LABEL_POST, signed, sigBytes);
		BdfList originalList = BdfList.of(POST.getInt(), text, sigBytes);
		byte[] originalBody = getRandomBytes(42);
		context.checking(new Expectations() {{
			oneOf(groupFactory).createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
					descriptor);
			will(returnValue(b.getGroup()));
			oneOf(blogFactory).parseBlog(b.getGroup());
			will(returnValue(b));
			oneOf(clientHelper).toByteArray(originalList);
			will(returnValue(originalBody));
			oneOf(messageFactory)
					.createMessage(group.getId(), message.getTimestamp(),
							originalBody);
			will(returnValue(message));
		}});
		BdfDictionary result =
				validator.validateMessage(message, group, m).getDictionary();
		assertEquals(authorList, result.getList(KEY_AUTHOR));
		assertEquals(rssFeed, result.getBoolean(KEY_RSS_FEED));
		context.assertIsSatisfied();
	}
	@Test
	public void testValidateProperWrappedComment()
			throws IOException, GeneralSecurityException {
		String comment = "This is another comment";
		MessageId originalId = new MessageId(getRandomId());
		MessageId oldId = new MessageId(getRandomId());
		byte[] sigBytes = getRandomBytes(42);
		MessageId currentId = new MessageId(getRandomId());
		BdfList m = BdfList.of(WRAPPED_COMMENT.getInt(), descriptor,
				message.getTimestamp(), comment, originalId, oldId, sigBytes,
				currentId);
		BdfList signed = BdfList.of(blog.getId(), message.getTimestamp(),
				comment, originalId, oldId);
		expectCrypto(blog, SIGNING_LABEL_COMMENT, signed, sigBytes);
		BdfList originalList = BdfList.of(COMMENT.getInt(), comment,
				originalId, oldId, sigBytes);
		byte[] originalBody = getRandomBytes(42);
		context.checking(new Expectations() {{
			oneOf(groupFactory).createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
					descriptor);
			will(returnValue(blog.getGroup()));
			oneOf(clientHelper).toByteArray(originalList);
			will(returnValue(originalBody));
			oneOf(messageFactory)
					.createMessage(group.getId(), message.getTimestamp(),
							originalBody);
			will(returnValue(message));
		}});
		BdfDictionary result =
				validator.validateMessage(message, group, m).getDictionary();
		assertEquals(comment, result.getString(KEY_COMMENT));
		assertEquals(authorList, result.getList(KEY_AUTHOR));
		assertEquals(
				message.getId().getBytes(), result.getRaw(KEY_ORIGINAL_MSG_ID));
		assertEquals(currentId.getBytes(), result.getRaw(KEY_PARENT_MSG_ID));
		context.assertIsSatisfied();
	}
	private void expectCrypto(Blog b, String label, BdfList signed, byte[] sig)
			throws IOException, GeneralSecurityException {
		context.checking(new Expectations() {{
			oneOf(blogFactory).parseBlog(group);
			will(returnValue(b));
			oneOf(clientHelper).toList(b.getAuthor());
			will(returnValue(authorList));
			oneOf(clientHelper)
					.verifySignature(sig, label, signed, author.getPublicKey());
		}});
	}
}
