package org.nodex.api.forum;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.PostHeader;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumPostHeader extends PostHeader {
	public ForumPostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, AuthorInfo authorInfo,
			boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
	}
}