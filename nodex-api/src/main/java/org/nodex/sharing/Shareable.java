package org.nodex.api.sharing;
import org.nodex.core.api.Nameable;
import org.nodex.core.api.sync.GroupId;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface Shareable extends Nameable {
	GroupId getId();
}