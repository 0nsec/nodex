package org.nodex.sharing;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.data.BdfList;
import org.nodex.core.api.data.MetadataEncoder;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.system.Clock;
import org.nodex.api.blog.BlogFactory;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import static org.nodex.core.util.ValidationUtils.checkSize;
@Immutable
@NotNullByDefault
class BlogSharingValidator extends SharingValidator {
	private final BlogFactory blogFactory;
	BlogSharingValidator(MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, BlogFactory blogFactory) {
		super(messageEncoder, clientHelper, metadataEncoder, clock);
		this.blogFactory = blogFactory;
	}
	@Override
	protected GroupId validateDescriptor(BdfList descriptor)
			throws FormatException {
		checkSize(descriptor, 2);
		BdfList authorList = descriptor.getList(0);
		boolean rssFeed = descriptor.getBoolean(1);
		Author author = clientHelper.parseAndValidateAuthor(authorList);
		if (rssFeed) return blogFactory.createFeedBlog(author).getId();
		else return blogFactory.createBlog(author).getId();
	}
}