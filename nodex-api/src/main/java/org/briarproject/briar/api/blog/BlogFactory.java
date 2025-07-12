package org.briarproject.briar.api.blog;
import org.briarproject.bramble.api.FormatException;
import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.sync.Group;
import org.briarproject.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface BlogFactory {
	Blog createBlog(Author author);
	Blog createFeedBlog(Author author);
	Blog parseBlog(Group g) throws FormatException;
}