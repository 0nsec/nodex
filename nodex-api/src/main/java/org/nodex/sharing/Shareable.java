package org.nodex.api.sharing;
import org.nodex.api.Nameable;
import org.nodex.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface Shareable extends Nameable {
	GroupId getId();
}