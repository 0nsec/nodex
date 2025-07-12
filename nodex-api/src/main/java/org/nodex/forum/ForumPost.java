package org.nodex.api.forum;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.ThreadedMessage;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumPost extends ThreadedMessage {
	public ForumPost(Message message, @Nullable MessageId parent,
			Author author) {
		super(message, parent, author);
	}
}