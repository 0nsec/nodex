package org.nodex.api.privategroup;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.sync.Group;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface PrivateGroupFactory {
	PrivateGroup createPrivateGroup(String name, Author creator);
	PrivateGroup createPrivateGroup(String name, Author creator, byte[] salt);
	PrivateGroup parsePrivateGroup(Group group) throws FormatException;
}