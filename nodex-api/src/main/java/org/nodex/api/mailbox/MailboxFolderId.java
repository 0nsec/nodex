package org.nodex.api.mailbox;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class MailboxFolderId {
    
    private final String id;
    
    public MailboxFolderId(String id) {
        this.id = id;
    }
    
    public String getString() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailboxFolderId)) return false;
        MailboxFolderId that = (MailboxFolderId) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return id;
    }
}
