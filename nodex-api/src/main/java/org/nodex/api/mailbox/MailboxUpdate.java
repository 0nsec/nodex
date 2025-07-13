package org.nodex.api.mailbox;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MailboxUpdate {
    int getVersion();
    byte[] getUpdate();
}
