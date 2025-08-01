package org.nodex.api.forum;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface ForumFactory {
	Forum createForum(String name);
	Forum createForum(String name, byte[] salt);
}