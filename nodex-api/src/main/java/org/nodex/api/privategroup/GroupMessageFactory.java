package org.nodex.api.privategroup;

import org.nodex.api.FormatException;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;

import javax.annotation.Nullable;
import java.security.GeneralSecurityException;

/**
 * Factory for creating private group messages.
 */
@NotNullByDefault
public interface GroupMessageFactory {

    /**
     * Creates a message for a private group.
     */
    Message createGroupMessage(GroupId groupId, long timestamp, 
                              @Nullable MessageId parentId, LocalAuthor author, 
                              String text) throws FormatException, GeneralSecurityException;

    /**
     * Creates a join message for a private group.
     */
    Message createJoinMessage(GroupId groupId, long timestamp,
                            LocalAuthor author) throws FormatException, GeneralSecurityException;

    /**
     * Creates a leave message for a private group.
     */
    Message createLeaveMessage(GroupId groupId, long timestamp,
                             LocalAuthor author) throws FormatException, GeneralSecurityException;
}
