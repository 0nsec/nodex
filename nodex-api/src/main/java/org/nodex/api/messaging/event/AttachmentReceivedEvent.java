package org.nodex.api.messaging.event;

import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.contact.ContactId;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

/**
 * Event fired when an attachment is received.
 */
@NotNullByDefault
public class AttachmentReceivedEvent {
    
    private final ContactId contactId;
    private final GroupId groupId;
    private final MessageId messageId;
    private final AttachmentHeader attachmentHeader;
    
    public AttachmentReceivedEvent(ContactId contactId, GroupId groupId, 
                                 MessageId messageId, AttachmentHeader attachmentHeader) {
        this.contactId = contactId;
        this.groupId = groupId;
        this.messageId = messageId;
        this.attachmentHeader = attachmentHeader;
    }
    
    public ContactId getContactId() {
        return contactId;
    }
    
    public GroupId getGroupId() {
        return groupId;
    }
    
    public MessageId getMessageId() {
        return messageId;
    }
    
    public AttachmentHeader getAttachmentHeader() {
        return attachmentHeader;
    }
}
