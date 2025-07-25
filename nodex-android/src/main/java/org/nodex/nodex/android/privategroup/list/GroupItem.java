package org.nodex.android.privategroup.list;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.privategroup.GroupMessageHeader;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import androidx.annotation.Nullable;
@Immutable
@NotNullByDefault
class GroupItem implements Comparable<GroupItem> {
	private final PrivateGroup privateGroup;
	private final AuthorInfo authorInfo;
	private final int messageCount, unreadCount;
	private final long timestamp;
	private final boolean dissolved;
	GroupItem(PrivateGroup privateGroup, AuthorInfo authorInfo,
			GroupCount count, boolean dissolved) {
		this.privateGroup = privateGroup;
		this.authorInfo = authorInfo;
		this.messageCount = count.getMsgCount();
		this.unreadCount = count.getUnreadCount();
		this.timestamp = count.getLatestMsgTime();
		this.dissolved = dissolved;
	}
	GroupItem(GroupItem item, GroupMessageHeader header) {
		this.privateGroup = item.privateGroup;
		this.authorInfo = item.authorInfo;
		this.messageCount = item.messageCount + 1;
		this.unreadCount = item.unreadCount + (header.isRead() ? 0 : 1);
		this.timestamp = Math.max(header.getTimestamp(), item.timestamp);
		this.dissolved = item.dissolved;
	}
	GroupItem(GroupItem item, boolean isDissolved) {
		this.privateGroup = item.privateGroup;
		this.authorInfo = item.authorInfo;
		this.messageCount = item.messageCount;
		this.unreadCount = item.unreadCount;
		this.timestamp = item.timestamp;
		this.dissolved = isDissolved;
	}
	GroupId getId() {
		return privateGroup.getId();
	}
	Author getCreator() {
		return privateGroup.getCreator();
	}
	AuthorInfo getCreatorInfo() {
		return authorInfo;
	}
	String getName() {
		return privateGroup.getName();
	}
	boolean isEmpty() {
		return messageCount == 0;
	}
	int getMessageCount() {
		return messageCount;
	}
	long getTimestamp() {
		return timestamp;
	}
	int getUnreadCount() {
		return unreadCount;
	}
	boolean isDissolved() {
		return dissolved;
	}
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
	@Override
	public boolean equals(@Nullable Object o) {
		return o instanceof GroupItem &&
				getId().equals(((GroupItem) o).getId());
	}
	@Override
	public int compareTo(GroupItem o) {
		if (this == o) return 0;
		long aTime = getTimestamp(), bTime = o.getTimestamp();
		if (aTime > bTime) return -1;
		if (aTime < bTime) return 1;
		String aName = getName();
		String bName = o.getName();
		return String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
	}
}