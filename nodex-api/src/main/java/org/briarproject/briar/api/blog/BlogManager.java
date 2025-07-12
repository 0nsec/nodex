package org.briarproject.briar.api.blog;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.db.Transaction;
import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.bramble.api.sync.ClientId;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
@NotNullByDefault
public interface BlogManager {
	ClientId CLIENT_ID = new ClientId("org.briarproject.briar.blog");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 0;
	void addBlog(Blog b) throws DbException;
	void addBlog(Transaction txn, Blog b) throws DbException;
	boolean canBeRemoved(Blog b) throws DbException;
	void removeBlog(Blog b) throws DbException;
	void removeBlog(Transaction txn, Blog b) throws DbException;
	void addLocalPost(BlogPost p) throws DbException;
	void addLocalPost(Transaction txn, BlogPost p) throws DbException;
	void addLocalComment(LocalAuthor author, GroupId groupId,
			@Nullable String comment, BlogPostHeader parentHeader)
			throws DbException;
	void addLocalComment(Transaction txn, LocalAuthor author,
			GroupId groupId, @Nullable String comment,
			BlogPostHeader parentHeader) throws DbException;
	Blog getBlog(GroupId g) throws DbException;
	Blog getBlog(Transaction txn, GroupId g) throws DbException;
	Collection<Blog> getBlogs(LocalAuthor localAuthor) throws DbException;
	Blog getPersonalBlog(Author author);
	Collection<Blog> getBlogs() throws DbException;
	Collection<Blog> getBlogs(Transaction txn) throws DbException;
	Collection<GroupId> getBlogIds(Transaction txn) throws DbException;
	BlogPostHeader getPostHeader(Transaction txn, GroupId g, MessageId m)
			throws DbException;
	String getPostText(MessageId m) throws DbException;
	String getPostText(Transaction txn, MessageId m) throws DbException;
	Collection<BlogPostHeader> getPostHeaders(GroupId g) throws DbException;
	List<BlogPostHeader> getPostHeaders(Transaction txn, GroupId g)
			throws DbException;
	void setReadFlag(MessageId m, boolean read) throws DbException;
	void setReadFlag(Transaction txn, MessageId m, boolean read) throws DbException;
	void registerRemoveBlogHook(RemoveBlogHook hook);
	interface RemoveBlogHook {
		void removingBlog(Transaction txn, Blog b) throws DbException;
	}
}