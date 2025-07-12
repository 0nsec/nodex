package org.nodex.api.forum;
import org.nodex.api.sync.Group;
import org.nodex.api.client.NamedGroup;
import org.nodex.api.sharing.Shareable;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class Forum extends NamedGroup implements Shareable {
	public Forum(Group group, String name, byte[] salt) {
		super(group, name, salt);
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof Forum && super.equals(o);
	}
}