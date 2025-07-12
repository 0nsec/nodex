package org.nodex.api.privategroup.event;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.privategroup.GroupMessageHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupMessageAddedEvent extends Event {
	private final GroupId groupId;
	private final GroupMessageHeader header;
	private final String text;
	private final boolean local;
	public GroupMessageAddedEvent(GroupId groupId, GroupMessageHeader header,
			String text, boolean local) {
		this.groupId = groupId;
		this.header = header;
		this.text = text;
		this.local = local;
	}
	public GroupId getGroupId() {
		return groupId;
	}
	public GroupMessageHeader getHeader() {
		return header;
	}
	public String getText() {
		return text;
	}
	public boolean isLocal() {
		return local;
	}
}