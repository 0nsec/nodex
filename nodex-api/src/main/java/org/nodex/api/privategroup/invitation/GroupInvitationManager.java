package org.nodex.api.privategroup.invitation;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.sync.ClientId;

import java.util.Collection;

/**
 * Manager for private group invitations.
 */
@NotNullByDefault
public interface GroupInvitationManager {

    ClientId CLIENT_ID = new ClientId("org.nodex.privategroup.invitation");

    /**
     * Sends an invitation to join a private group.
     */
    void sendInvitation(PrivateGroup privateGroup, ContactId contactId, 
                       String message) throws DbException;

    /**
     * Sends an invitation to join a private group.
     */
    void sendInvitation(Transaction txn, PrivateGroup privateGroup, 
                       ContactId contactId, String message) throws DbException;

    /**
     * Responds to a private group invitation.
     */
    void respondToInvitation(ContactId contactId, PrivateGroup privateGroup, 
                           boolean accept) throws DbException;

    /**
     * Responds to a private group invitation.
     */
    void respondToInvitation(Transaction txn, ContactId contactId, 
                           PrivateGroup privateGroup, boolean accept) throws DbException;

    /**
     * Returns all invitations for a contact.
     */
    Collection<GroupInvitationRequest> getInvitations(ContactId contactId) throws DbException;

    /**
     * Returns all invitations for a contact.
     */
    Collection<GroupInvitationRequest> getInvitations(Transaction txn, 
                                                     ContactId contactId) throws DbException;
}
