package org.nodex.api.privategroup;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.client.NamedGroup;
import org.nodex.api.sharing.Shareable;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class PrivateGroup extends NamedGroup implements Shareable {
	private final Author creator;
	public PrivateGroup(Group group, String name, Author creator, byte[] salt) {
		super(group, name, salt);
		this.creator = creator;
	}
	public Author getCreator() {
		return creator;
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof PrivateGroup && super.equals(o);
	}
}