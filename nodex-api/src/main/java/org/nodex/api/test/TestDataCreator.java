package org.nodex.api.test;

import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Interface for creating test data.
 */
@NotNullByDefault
public interface TestDataCreator {
    
    /**
     * Creates a test contact.
     */
    Contact createContact();
    
    /**
     * Creates a test contact with a specific ID.
     */
    Contact createContact(ContactId contactId);
    
    /**
     * Creates a test local author.
     */
    LocalAuthor createLocalAuthor();
    
    /**
     * Creates a test local author with a specific name.
     */
    LocalAuthor createLocalAuthor(String name);
}
