package org.nodex.api.client;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class ThreadedMessage {
	private final Message message;
	@Nullable
	private final MessageId parent;
	private final Author author;
	public ThreadedMessage(Message message, @Nullable MessageId parent,
			Author author) {
		this.message = message;
		this.parent = parent;
		this.author = author;
	}
	public Message getMessage() {
		return message;
	}
	@Nullable
	public MessageId getParent() {
		return parent;
	}
	public Author getAuthor() {
		return author;
	}
}