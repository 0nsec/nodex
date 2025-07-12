package org.nodex.android.threaded;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.client.MessageTree.MessageNode;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import static org.nodex.android.threaded.ThreadItemAdapter.UNDEFINED;
import static org.nodex.android.util.UiUtils.getContactDisplayName;
@NotThreadSafe
@NotNullByDefault
public abstract class ThreadItem implements MessageNode {
	private final MessageId messageId;
	@Nullable
	private final MessageId parentId;
	private final String text;
	private final long timestamp;
	private final Author author;
	private final AuthorInfo authorInfo;
	private int level = UNDEFINED;
	private boolean isRead, highlighted;
	public ThreadItem(MessageId messageId, @Nullable MessageId parentId,
			String text, long timestamp, Author author, AuthorInfo authorInfo,
			boolean isRead) {
		this.messageId = messageId;
		this.parentId = parentId;
		this.text = text;
		this.timestamp = timestamp;
		this.author = author;
		this.authorInfo = authorInfo;
		this.isRead = isRead;
		this.highlighted = false;
	}
	public String getText() {
		return text;
	}
	public int getLevel() {
		return level;
	}
	@Override
	public MessageId getId() {
		return messageId;
	}
	@Override
	@Nullable
	public MessageId getParentId() {
		return parentId;
	}
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	public Author getAuthor() {
		return author;
	}
	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}
	public String getAuthorName() {
		return getContactDisplayName(author, authorInfo.getAlias());
	}
	@Override
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean read) {
		isRead = read;
	}
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
	public boolean isHighlighted() {
		return highlighted;
	}
	@Override
	public int hashCode() {
		return messageId.hashCode();
	}
	@Override
	public boolean equals(@Nullable Object o) {
		return o instanceof ThreadItem &&
				messageId.equals(((ThreadItem) o).messageId);
	}
}