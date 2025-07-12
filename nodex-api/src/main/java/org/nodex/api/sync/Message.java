package org.nodex.api.sync;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A message within a group
 */
@NotNullByDefault
public class Message {
    private final MessageId id;
    private final GroupId groupId;
    private final long timestamp;
    private final byte[] body;

    public Message(MessageId id, GroupId groupId, long timestamp, byte[] body) {
        if (id == null) throw new IllegalArgumentException("Message ID cannot be null");
        if (groupId == null) throw new IllegalArgumentException("Group ID cannot be null");
        if (body == null) throw new IllegalArgumentException("Body cannot be null");
        this.id = id;
        this.groupId = groupId;
        this.timestamp = timestamp;
        this.body = body.clone();
    }

    public MessageId getId() {
        return id;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getBody() {
        return body.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", groupId=" + groupId + ", timestamp=" + timestamp + '}';
    }
}
