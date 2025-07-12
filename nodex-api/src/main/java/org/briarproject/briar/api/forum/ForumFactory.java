package org.briarproject.briar.api.forum;
import org.briarproject.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface ForumFactory {
	Forum createForum(String name);
	Forum createForum(String name, byte[] salt);
}