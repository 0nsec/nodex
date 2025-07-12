package org.nodex.api.client;
import org.nodex.core.api.sync.Group;
import org.nodex.core.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class BaseGroup {
	private final Group group;
	public BaseGroup(Group group) {
		this.group = group;
	}
	public GroupId getId() {
		return group.getId();
	}
	public Group getGroup() {
		return group;
	}
	@Override
	public int hashCode() {
		return group.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof BaseGroup &&
				getGroup().equals(((BaseGroup) o).getGroup());
	}
}