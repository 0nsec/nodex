package org.nodex.api.privategroup;

import org.nodex.api.identity.Author;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

/**
 * A message in a private group.
 */
@NotNullByDefault
public class GroupMessage {
    
    private final MessageId id;
    private final GroupId groupId;
    private final Author author;
    private final String text;
    private final long timestamp;
    private final MessageType type;
    
    public GroupMessage(MessageId id, GroupId groupId, Author author, 
                       String text, long timestamp, MessageType type) {
        this.id = id;
        this.groupId = groupId;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    public MessageId getId() {
        return id;
    }
    
    public GroupId getGroupId() {
        return groupId;
    }
    
    public Author getAuthor() {
        return author;
    }
    
    public String getText() {
        return text;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
}
