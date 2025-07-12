package org.nodex.api.blog.event;
import org.nodex.api.event.Event;
import org.nodex.api.sync.GroupId;
import org.nodex.api.blog.BlogPostHeader;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class BlogPostAddedEvent extends Event {
	private final GroupId groupId;
	private final BlogPostHeader header;
	private final boolean local;
	public BlogPostAddedEvent(GroupId groupId, BlogPostHeader header,
			boolean local) {
		this.groupId = groupId;
		this.header = header;
		this.local = local;
	}
	public GroupId getGroupId() {
		return groupId;
	}
	public BlogPostHeader getHeader() {
		return header;
	}
	public boolean isLocal() {
		return local;
	}
}