package org.nodex.api.privategroup;

import org.nodex.api.contact.ContactId;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Information about a member of a private group.
 */
@Immutable
@NotNullByDefault
public class GroupMember {

    public enum Visibility {
        VISIBLE,
        REVEALED_BY_CONTACT,
        REVEALED_BY_US,
        INVISIBLE
    }

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

    /**
     * Returns the ContactId of a visible contact
     * or null if the contact is not visible or the member is no contact.
     */
    @Nullable
    public ContactId getContactId() {
        return contactId;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMember that = (GroupMember) o;
        return author.equals(that.author);
    }

    @Override
    public int hashCode() {
        return author.hashCode();
    }

    @Override
    public String toString() {
        return "GroupMember{author=" + author.getName() + ", isCreator=" + isCreator + 
               ", visibility=" + visibility + '}';
    }
}
