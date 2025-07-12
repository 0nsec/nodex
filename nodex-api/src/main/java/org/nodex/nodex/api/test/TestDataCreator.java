package org.nodex.api.test;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.lifecycle.IoExecutor;
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