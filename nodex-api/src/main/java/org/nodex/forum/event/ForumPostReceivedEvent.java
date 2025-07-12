package org.nodex.api.forum.event;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.forum.ForumPostHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class ForumPostReceivedEvent extends Event {
	private final GroupId groupId;
	private final ForumPostHeader header;
	private final String text;
	public ForumPostReceivedEvent(GroupId groupId, ForumPostHeader header,
			String text) {
		this.groupId = groupId;
		this.header = header;
		this.text = text;
	}
	public GroupId getGroupId() {
		return groupId;
	}
	public ForumPostHeader getHeader() {
		return header;
	}
	public String getText() {
		return text;
	}
}