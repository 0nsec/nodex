package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.Author;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
@Immutable
@NotNullByDefault
class BlogMessageParserImpl extends MessageParserImpl<Blog> {
	private final BlogFactory blogFactory;
	@Inject
	BlogMessageParserImpl(ClientHelper clientHelper, BlogFactory blogFactory) {
		super(clientHelper);
		this.blogFactory = blogFactory;
	}
	@Override
	public Blog createShareable(BdfList descriptor) throws FormatException {
		BdfList authorList = descriptor.getList(0);
		boolean rssFeed = descriptor.getBoolean(1);
		Author author = clientHelper.parseAndValidateAuthor(authorList);
		if (rssFeed) return blogFactory.createFeedBlog(author);
		else return blogFactory.createBlog(author);
	}
}
