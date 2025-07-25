package org.nodex.api.client;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.MessageId;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorInfo.Status;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class PostHeader {
	private final MessageId id;
	@Nullable
	private final MessageId parentId;
	private final long timestamp;
	private final Author author;
	private final AuthorInfo authorInfo;
	private final boolean read;
	public PostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, AuthorInfo authorInfo, boolean read) {
		this.id = id;
		this.parentId = parentId;
		this.timestamp = timestamp;
		this.author = author;
		this.authorInfo = authorInfo;
		this.read = read;
	}
	public MessageId getId() {
		return id;
	}
	public Author getAuthor() {
		return author;
	}
	public Status getAuthorStatus() {
		return authorInfo.getStatus();
	}
	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public boolean isRead() {
		return read;
	}
	@Nullable
	public MessageId getParentId() {
		return parentId;
	}
}