package org.nodex.api.sharing;
import org.nodex.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public abstract class InvitationItem<S extends Shareable> {
	private final S shareable;
	private final boolean subscribed;
	public InvitationItem(S shareable, boolean subscribed) {
		this.shareable = shareable;
		this.subscribed = subscribed;
	}
	public S getShareable() {
		return shareable;
	}
	public GroupId getId() {
		return shareable.getId();
	}
	public String getName() {
		return shareable.getName();
	}
	public boolean isSubscribed() {
		return subscribed;
	}
}