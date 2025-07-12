package org.nodex.api.privategroup;
import org.nodex.api.FormatException;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
@NotNullByDefault
public interface PrivateGroupManager {
	ClientId CLIENT_ID = new ClientId("org.nodex.privategroup");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 0;
	void addPrivateGroup(PrivateGroup group, GroupMessage joinMsg,
			boolean creator) throws DbException;
	void addPrivateGroup(Transaction txn, PrivateGroup group,
			GroupMessage joinMsg, boolean creator) throws DbException;
	void removePrivateGroup(Transaction txn, GroupId g) throws DbException;
	void removePrivateGroup(GroupId g) throws DbException;
	MessageId getPreviousMsgId(Transaction txn, GroupId g) throws DbException;
	MessageId getPreviousMsgId(GroupId g) throws DbException;
	void markGroupDissolved(Transaction txn, GroupId g) throws DbException;
	boolean isDissolved(Transaction txn, GroupId g) throws DbException;
	boolean isDissolved(GroupId g) throws DbException;
	GroupMessageHeader addLocalMessage(GroupMessage p) throws DbException;
	GroupMessageHeader addLocalMessage(Transaction txn, GroupMessage p)
			throws DbException;
	PrivateGroup getPrivateGroup(GroupId g) throws DbException;
	PrivateGroup getPrivateGroup(Transaction txn, GroupId g) throws DbException;
	Collection<PrivateGroup> getPrivateGroups() throws DbException;
	Collection<PrivateGroup> getPrivateGroups(Transaction txn)
			throws DbException;
	boolean isOurPrivateGroup(Transaction txn, PrivateGroup g)
			throws DbException;
	String getMessageText(MessageId m) throws DbException;
	String getMessageText(Transaction txn, MessageId m) throws DbException;
	Collection<GroupMessageHeader> getHeaders(GroupId g) throws DbException;
	List<GroupMessageHeader> getHeaders(Transaction txn, GroupId g)
			throws DbException;
	Collection<GroupMember> getMembers(GroupId g) throws DbException;
	Collection<GroupMember> getMembers(Transaction txn, GroupId g)
			throws DbException;
	boolean isMember(Transaction txn, GroupId g, Author a) throws DbException;
	GroupCount getGroupCount(Transaction txn, GroupId g) throws DbException;
	GroupCount getGroupCount(GroupId g) throws DbException;
	void setReadFlag(Transaction txn, GroupId g, MessageId m, boolean read)
			throws DbException;
	void setReadFlag(GroupId g, MessageId m, boolean read) throws DbException;
	void relationshipRevealed(Transaction txn, GroupId g, AuthorId a,
			boolean byContact) throws FormatException, DbException;
	void registerPrivateGroupHook(PrivateGroupHook hook);
	@NotNullByDefault
	interface PrivateGroupHook {
		void addingMember(Transaction txn, GroupId g, Author a)
				throws DbException;
		void removingGroup(Transaction txn, GroupId g) throws DbException;
	}
}