package org.nodex.introduction;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class RequestMessage extends AbstractIntroductionMessage {
	private final Author author;
	@Nullable
	private final String text;
	RequestMessage(MessageId messageId, GroupId groupId, long timestamp,
			@Nullable MessageId previousMessageId, Author author,
			@Nullable String text, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.author = author;
		this.text = text;
	}
	public Author getAuthor() {
		return author;
	}
	@Nullable
	public String getText() {
		return text;
	}
}
