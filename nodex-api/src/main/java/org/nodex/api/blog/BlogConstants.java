package org.nodex.api.blog;
import static org.nodex.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
public interface BlogConstants {
	int MAX_BLOG_POST_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 1024;
	int MAX_BLOG_COMMENT_TEXT_LENGTH = MAX_BLOG_POST_TEXT_LENGTH;
	String KEY_TYPE = "type";
	String KEY_TIMESTAMP = "timestamp";
	String KEY_TIME_RECEIVED = "timeReceived";
	String KEY_AUTHOR = "author";
	String KEY_RSS_FEED = "rssFeed";
	String KEY_READ = "read";
	String KEY_COMMENT = "comment";
	String KEY_ORIGINAL_MSG_ID = "originalMessageId";
	String KEY_ORIGINAL_PARENT_MSG_ID = "originalParentMessageId";
	String KEY_PARENT_MSG_ID = "parentMessageId";
}