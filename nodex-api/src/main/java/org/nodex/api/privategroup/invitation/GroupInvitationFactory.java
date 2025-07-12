package org.nodex.api.privategroup.invitation;

import org.nodex.api.FormatException;
import org.nodex.api.contact.ContactId;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;

import java.security.GeneralSecurityException;

/**
 * Factory for creating private group invitation messages.
 */
@NotNullByDefault
public interface GroupInvitationFactory {

    /**
     * Creates an invitation message for a private group.
     */
    Message createInviteMessage(GroupId groupId, long timestamp,
                              PrivateGroup privateGroup, String text,
                              LocalAuthor author) throws FormatException, GeneralSecurityException;

    /**
     * Creates a join message for accepting a private group invitation.
     */
    Message createJoinMessage(GroupId groupId, long timestamp,
                            PrivateGroup privateGroup, LocalAuthor author) 
                            throws FormatException, GeneralSecurityException;

    /**
     * Creates a leave message for declining a private group invitation.
     */
    Message createLeaveMessage(GroupId groupId, long timestamp,
                             PrivateGroup privateGroup, LocalAuthor author) 
                             throws FormatException, GeneralSecurityException;
}
