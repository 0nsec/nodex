package org.nodex.api.identity;

import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Information about an author including verification status
 */
@NotNullByDefault
public class AuthorInfo {
    
    public enum Status {
        OURSELVES,
        VERIFIED,
        UNVERIFIED,
        UNKNOWN,
        NONE
    }

    private final Status status;
    private final String alias;
    private final AttachmentHeader avatarHeader;

    public AuthorInfo(Status status, String alias, AttachmentHeader avatarHeader) {
        this.status = status != null ? status : Status.UNKNOWN;
        this.alias = alias;
        this.avatarHeader = avatarHeader;
    }

    public AuthorInfo(Status status) {
        this(status, null, null);
    }

    public Status getStatus() {
        return status;
    }

    public String getAlias() {
        return alias;
    }

    public AttachmentHeader getAvatarHeader() {
        return avatarHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorInfo that = (AuthorInfo) o;
        return status == that.status && 
               (alias != null ? alias.equals(that.alias) : that.alias == null) &&
               (avatarHeader != null ? avatarHeader.equals(that.avatarHeader) : that.avatarHeader == null);
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (avatarHeader != null ? avatarHeader.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthorInfo{status=" + status + ", alias='" + alias + "', avatarHeader=" + avatarHeader + '}';
    }
}
