package org.nodex.api.blog;
import org.nodex.api.system.FormatException;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface BlogFactory {
	Blog createBlog(Author author);
	Blog createFeedBlog(Author author);
	Blog parseBlog(Group g) throws FormatException;
}