package org.nodex.api.privategroup;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.PostHeader;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupMessageHeader extends PostHeader {
	private final GroupId groupId;
	public GroupMessageHeader(GroupId groupId, MessageId id,
			@Nullable MessageId parentId, long timestamp,
			Author author, AuthorInfo authorInfo, boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
		this.groupId = groupId;
	}
	public GroupId getGroupId() {
		return groupId;
	}
}