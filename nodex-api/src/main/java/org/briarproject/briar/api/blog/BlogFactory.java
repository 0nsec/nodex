package org.nodex.api.blog;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.Group;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface BlogFactory {
	Blog createBlog(Author author);
	Blog createFeedBlog(Author author);
	Blog parseBlog(Group g) throws FormatException;
}