package org.nodex.api.contact;

import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * Manager for handling contacts and contact operations.
 */
@NotNullByDefault
public interface ContactManager {
    /**
     * Add a contact.
     */
    void addContact(Author author, AuthorId localAuthorId, boolean verified);
    
    /**
     * Get all contacts.
     */
    Collection<Contact> getContacts();
    
    /**
     * Get a contact by author ID.
     */
    Contact getContact(AuthorId authorId);
    
    /**
     * Remove a contact.
     */
    void removeContact(AuthorId authorId);
    
    /**
     * Hook interface for contact events.
     */
    interface ContactHook {
        /**
         * Called when a contact is added.
         */
        void onContactAdded(Contact contact);
        
        /**
         * Called when a contact is removed.
         */
        void onContactRemoved(Contact contact);
        
        /**
         * Called when a contact is updated.
         */
        void onContactUpdated(Contact contact);
    }
}
