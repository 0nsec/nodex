package org.nodex.api.introduction;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.ClientId;

import java.util.Collection;

/**
 * Manages the introduction protocol between contacts.
 */
@NotNullByDefault
public interface IntroductionManager {

    /**
     * Client ID for the introduction protocol.
     */
    ClientId CLIENT_ID = new ClientId("org.nodex.introduction");

    /**
     * Major version of the introduction protocol.
     */
    int MAJOR_VERSION = 1;

    /**
     * Minor version of the introduction protocol.
     */
    int MINOR_VERSION = 0;

    /**
     * Sends an introduction request to a contact.
     * 
     * @param contactId The contact to send the request to
     * @param text The introduction text
     * @throws DbException if a database error occurs
     */
    void sendIntroductionRequest(ContactId contactId, String text) throws DbException;

    /**
     * Accepts an introduction request.
     * 
     * @param contactId The contact who sent the request
     * @throws DbException if a database error occurs
     */
    void acceptIntroductionRequest(ContactId contactId) throws DbException;

    /**
     * Declines an introduction request.
     * 
     * @param contactId The contact who sent the request
     * @throws DbException if a database error occurs
     */
    void declineIntroductionRequest(ContactId contactId) throws DbException;

    /**
     * Gets the client ID for the introduction protocol.
     * 
     * @return The client ID
     */
    ClientId getClientId();
}
