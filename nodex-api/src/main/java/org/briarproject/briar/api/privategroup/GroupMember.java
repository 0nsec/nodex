package org.nodex.api.privategroup;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.identity.Author;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupMember {
	private final Author author;
	private final AuthorInfo authorInfo;
	private final boolean isCreator;
	@Nullable
	private final ContactId contactId;
	private final Visibility visibility;
	public GroupMember(Author author, AuthorInfo authorInfo, boolean isCreator,
			@Nullable ContactId contactId, Visibility visibility) {
		this.author = author;
		this.authorInfo = authorInfo;
		this.isCreator = isCreator;
		this.contactId = contactId;
		this.visibility = visibility;
	}
	public Author getAuthor() {
		return author;
	}
	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}
	public boolean isCreator() {
		return isCreator;
	}
	@Nullable
	public ContactId getContactId() {
		return contactId;
	}
	public Visibility getVisibility() {
		return visibility;
	}
}