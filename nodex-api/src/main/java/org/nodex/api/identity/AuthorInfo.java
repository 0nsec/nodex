package org.nodex.api.identity;

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
        UNKNOWN
    }

    private final Status status;
    private final String alias;

    public AuthorInfo(Status status, String alias) {
        this.status = status != null ? status : Status.UNKNOWN;
        this.alias = alias;
    }

    public AuthorInfo(Status status) {
        this(status, null);
    }

    public Status getStatus() {
        return status;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorInfo that = (AuthorInfo) o;
        return status == that.status && 
               (alias != null ? alias.equals(that.alias) : that.alias == null);
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthorInfo{status=" + status + ", alias='" + alias + "'}";
    }
}
