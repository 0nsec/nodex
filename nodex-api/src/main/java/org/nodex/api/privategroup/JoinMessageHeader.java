package org.nodex.api.privategroup;

import org.nodex.api.identity.Author;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

/**
 * Header for a join message in a private group.
 */
@NotNullByDefault
public class JoinMessageHeader {
    
    private final MessageId id;
    private final GroupId groupId;
    private final Author author;
    private final long timestamp;
    
    public JoinMessageHeader(MessageId id, GroupId groupId, Author author, long timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.author = author;
        this.timestamp = timestamp;
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
    
    public long getTimestamp() {
        return timestamp;
    }
}
