package org.nodex.android.privategroup.memberlist;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.identity.Author;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorInfo.Status;
import org.nodex.api.privategroup.GroupMember;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
@NotThreadSafe
@NotNullByDefault
class MemberListItem {
	private final GroupMember groupMember;
	private boolean online;
	MemberListItem(GroupMember groupMember, boolean online) {
		this.groupMember = groupMember;
		this.online = online;
	}
	Author getMember() {
		return groupMember.getAuthor();
	}
	AuthorInfo getAuthorInfo() {
		return groupMember.getAuthorInfo();
	}
	Status getStatus() {
		return groupMember.getAuthorInfo().getStatus();
	}
	boolean isCreator() {
		return groupMember.isCreator();
	}
	@Nullable
	ContactId getContactId() {
		return groupMember.getContactId();
	}
	boolean isOnline() {
		return online;
	}
	void setOnline(boolean online) {
		this.online = online;
	}
}