package org.nodex.api.messaging.event;

import org.nodex.api.contact.ContactId;
import org.nodex.api.event.Event;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

@NotNullByDefault
public class PrivateMessageReceivedEvent extends Event {
    
    private final ContactId contactId;
    private final GroupId groupId;
    private final MessageId messageId;
    private final String text;
    private final long timestamp;
    
    public PrivateMessageReceivedEvent(ContactId contactId, GroupId groupId, MessageId messageId, String text, long timestamp) {
        this.contactId = contactId;
        this.groupId = groupId;
        this.messageId = messageId;
        this.text = text;
        this.timestamp = timestamp;
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
    
    public String getText() {
        return text;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
