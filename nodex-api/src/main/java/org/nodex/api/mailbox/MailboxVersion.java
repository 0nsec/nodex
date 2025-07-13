package org.nodex.api.mailbox;

import org.nodex.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface MailboxVersion {
    int getMajor();
    int getMinor();
}
