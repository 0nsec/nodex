package org.nodex.api.mailbox;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class MailboxAuthToken {
    
    private final byte[] token;
    
    public MailboxAuthToken(byte[] token) {
        this.token = token.clone();
    }
    
    public byte[] getToken() {
        return token.clone();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailboxAuthToken)) return false;
        MailboxAuthToken that = (MailboxAuthToken) o;
        return java.util.Arrays.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(token);
    }
}
