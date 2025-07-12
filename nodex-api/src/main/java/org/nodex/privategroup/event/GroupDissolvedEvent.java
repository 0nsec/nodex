package org.nodex.api.privategroup.event;
import org.nodex.api.event.Event;
import org.nodex.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupDissolvedEvent extends Event {
	private final GroupId groupId;
	public GroupDissolvedEvent(GroupId groupId) {
		this.groupId = groupId;
	}
	public GroupId getGroupId() {
		return groupId;
	}
}