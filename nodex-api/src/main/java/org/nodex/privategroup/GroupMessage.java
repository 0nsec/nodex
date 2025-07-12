package org.nodex.api.privategroup;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.client.ThreadedMessage;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupMessage extends ThreadedMessage {
	public GroupMessage(Message message, @Nullable MessageId parent,
			Author member) {
		super(message, parent, member);
	}
	public Author getMember() {
		return super.getAuthor();
	}
}