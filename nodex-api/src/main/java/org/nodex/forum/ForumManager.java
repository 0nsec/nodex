package org.nodex.api.forum;
import org.nodex.api.crypto.CryptoExecutor;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
@NotNullByDefault
public interface ForumManager {
	ClientId CLIENT_ID = new ClientId("org.nodex.forum");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 0;
	Forum addForum(String name) throws DbException;
	void addForum(Transaction txn, Forum f) throws DbException;
	void removeForum(Forum f) throws DbException;
	void removeForum(Transaction txn, Forum f) throws DbException;
	@CryptoExecutor
	ForumPost createLocalPost(GroupId groupId, String text, long timestamp,
			@Nullable MessageId parentId, LocalAuthor author);
	ForumPostHeader addLocalPost(ForumPost p) throws DbException;
	ForumPostHeader addLocalPost(Transaction txn, ForumPost p)
			throws DbException;
	Forum getForum(GroupId g) throws DbException;
	Forum getForum(Transaction txn, GroupId g) throws DbException;
	Collection<Forum> getForums() throws DbException;
	Collection<Forum> getForums(Transaction txn) throws DbException;
	String getPostText(MessageId m) throws DbException;
	String getPostText(Transaction txn, MessageId m) throws DbException;
	Collection<ForumPostHeader> getPostHeaders(GroupId g) throws DbException;
	List<ForumPostHeader> getPostHeaders(Transaction txn, GroupId g)
			throws DbException;
	void registerRemoveForumHook(RemoveForumHook hook);
	GroupCount getGroupCount(GroupId g) throws DbException;
	GroupCount getGroupCount(Transaction txn, GroupId g) throws DbException;
	void setReadFlag(GroupId g, MessageId m, boolean read) throws DbException;
	void setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read) throws DbException;
	interface RemoveForumHook {
		void removingForum(Transaction txn, Forum f) throws DbException;
	}
}