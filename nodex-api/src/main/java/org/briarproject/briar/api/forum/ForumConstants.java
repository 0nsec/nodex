package org.briarproject.briar.api.forum;
import static org.briarproject.bramble.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
public interface ForumConstants {
	int MAX_FORUM_NAME_LENGTH = 100;
	int FORUM_SALT_LENGTH = 32;
	int MAX_FORUM_POST_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 1024;
	String KEY_TIMESTAMP = "timestamp";
	String KEY_PARENT = "parent";
	String KEY_AUTHOR = "author";
	String KEY_LOCAL = "local";
	String KEY_READ = "read";
}