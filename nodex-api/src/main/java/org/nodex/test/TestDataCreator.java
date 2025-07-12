package org.nodex.api.test;
import org.nodex.api.contact.Contact;
import org.nodex.api.db.DbException;
import org.nodex.api.lifecycle.IoExecutor;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface TestDataCreator {
	void createTestData(int numContacts, int numPrivateMsgs, int avatarPercent,
			int numBlogPosts, int numForums, int numForumPosts,
			int numPrivateGroups, int numPrivateGroupMessages);
	@IoExecutor
	Contact addContact(String name, boolean alias, boolean avatar)
			throws DbException;
}