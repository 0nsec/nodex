package org.nodex.api.mailbox;

import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

import java.util.Collection;

/**
 * Manages mailbox functionality for store-and-forward messaging.
 */
@NotNullByDefault
public interface MailboxManager extends Service {
    
    /**
     * Check for new messages in the mailbox.
     */
    void checkMailbox();
    
    /**
     * Send a message through the mailbox system.
     */
    void sendViaMailbox(ContactId contactId, byte[] message);
    
    /**
     * Get pending messages for a contact.
     */
    Collection<byte[]> getPendingMessages(ContactId contactId);
}
