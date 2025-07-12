package org.briarproject.briar.api.forum;
import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.bramble.api.sync.ClientId;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.briar.api.client.MessageTracker.GroupCount;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
@NotNullByDefault
public interface ForumManager {
	ClientId CLIENT_ID = new ClientId("org.briarproject.briar.forum");
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