package org.nodex.api.blog;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.forum.ForumPost;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class BlogPost extends ForumPost {
	public BlogPost(Message message, @Nullable MessageId parent,
			Author author) {
		super(message, parent, author);
	}
}