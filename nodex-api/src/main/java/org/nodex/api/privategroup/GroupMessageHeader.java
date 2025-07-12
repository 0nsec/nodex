package org.nodex.api.privategroup;

import org.nodex.api.client.PostHeader;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.MessageId;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Header for a private group message.
 */
@Immutable
@NotNullByDefault
public class GroupMessageHeader extends PostHeader {

    private final String text;
    private final boolean isPost;

    public GroupMessageHeader(MessageId id, @Nullable MessageId parentId, 
                            long timestamp, Author author, AuthorInfo authorInfo, 
                            boolean read, String text, boolean isPost) {
        super(id, parentId, timestamp, author, authorInfo, read);
        this.text = text;
        this.isPost = isPost;
    }

    public String getText() {
        return text;
    }

    public boolean isPost() {
        return isPost;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GroupMessageHeader && super.equals(o);
    }

    @Override
    public String toString() {
        return "GroupMessageHeader{id=" + getId() + ", timestamp=" + getTimestamp() + 
               ", author=" + getAuthor().getName() + ", text='" + text + "'}";
    }
}
