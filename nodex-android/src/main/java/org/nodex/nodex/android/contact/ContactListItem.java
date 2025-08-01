package org.nodex.android.contact;
import org.nodex.core.api.contact.Contact;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ContactListItem extends ContactItem
		implements Comparable<ContactListItem> {
	private final boolean empty;
	private final long timestamp;
	private final int unread;
	public ContactListItem(Contact contact, AuthorInfo authorInfo,
			boolean connected, GroupCount count) {
		super(contact, authorInfo, connected);
		this.empty = count.getMsgCount() == 0;
		this.unread = count.getUnreadCount();
		this.timestamp = count.getLatestMsgTime();
	}
	private ContactListItem(Contact contact, AuthorInfo authorInfo,
			boolean connected, boolean empty, int unread, long timestamp) {
		super(contact, authorInfo, connected);
		this.empty = empty;
		this.timestamp = timestamp;
		this.unread = unread;
	}
	ContactListItem(ContactListItem item, boolean connected) {
		this(item.getContact(), item.getAuthorInfo(), connected, item.empty,
				item.unread, item.timestamp);
	}
	ContactListItem(ContactListItem item, long timestamp, boolean read) {
		this(item.getContact(), item.getAuthorInfo(), item.isConnected(), false,
				read ? item.unread : item.unread + 1,
				Math.max(timestamp, item.timestamp));
	}
	ContactListItem(ContactListItem item, @Nullable String alias) {
		this(update(item.getContact(), alias), item.getAuthorInfo(),
				item.isConnected(), item.empty, item.unread, item.timestamp);
	}
	private static Contact update(Contact c, @Nullable String alias) {
		return new Contact(c.getId(), c.getAuthor(), c.getLocalAuthorId(),
				alias, c.getHandshakePublicKey(), c.isVerified());
	}
	ContactListItem(ContactListItem item, AttachmentHeader attachmentHeader) {
		this(item.getContact(), new AuthorInfo(item.getAuthorInfo().getStatus(),
						item.getAuthorInfo().getAlias(), attachmentHeader),
				item.isConnected(), item.empty, item.unread, item.timestamp);
	}
	boolean isEmpty() {
		return empty;
	}
	long getTimestamp() {
		return timestamp;
	}
	int getUnreadCount() {
		return unread;
	}
	@Override
	public int compareTo(ContactListItem o) {
		return Long.compare(o.getTimestamp(), timestamp);
	}
}