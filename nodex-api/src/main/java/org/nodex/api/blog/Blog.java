package org.nodex.api.blog;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.client.BaseGroup;
import org.nodex.api.sharing.Shareable;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class Blog extends BaseGroup implements Shareable {
	private final Author author;
	private final boolean rssFeed;
	public Blog(Group group, Author author, boolean rssFeed) {
		super(group);
		this.author = author;
		this.rssFeed = rssFeed;
	}
	public Author getAuthor() {
		return author;
	}
	public boolean isRssFeed() {
		return rssFeed;
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof Blog && super.equals(o);
	}
	@Override
	public String getName() {
		return author.getName();
	}
}