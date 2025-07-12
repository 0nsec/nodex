package org.briarproject.briar.api.feed;
import org.briarproject.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class RssProperties {
	@Nullable
	private final String url, title, description, author, link, uri;
	public RssProperties(@Nullable String url, @Nullable String title,
			@Nullable String description, @Nullable String author,
			@Nullable String link, @Nullable String uri) {
		this.url = url;
		this.title = title;
		this.description = description;
		this.author = author;
		this.link = link;
		this.uri = uri;
	}
	@Nullable
	public String getUrl() {
		return url;
	}
	@Nullable
	public String getTitle() {
		return title;
	}
	@Nullable
	public String getDescription() {
		return description;
	}
	@Nullable
	public String getAuthor() {
		return author;
	}
	@Nullable
	public String getLink() {
		return link;
	}
	@Nullable
	public String getUri() {
		return uri;
	}
}