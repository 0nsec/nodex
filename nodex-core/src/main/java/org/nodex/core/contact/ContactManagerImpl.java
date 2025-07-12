package org.nodex.core.contact;

import org.nodex.api.contact.ContactManager;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.db.ContactExistsException;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.event.EventBus;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Implementation of ContactManager that manages contacts.
 */
@ThreadSafe
@NotNullByDefault
public class ContactManagerImpl implements ContactManager, Service {

    private static final Logger LOG = Logger.getLogger(ContactManagerImpl.class.getName());

    private final DatabaseComponent db;
    private final EventBus eventBus;
    private final ConcurrentMap<ContactId, Contact> contacts;
    private volatile boolean started = false;

    @Inject
    public ContactManagerImpl(DatabaseComponent db, EventBus eventBus) {
        this.db = db;
        this.eventBus = eventBus;
        this.contacts = new ConcurrentHashMap<>();
    }

    @Override
    public void startService() throws ServiceException {
        LOG.info("Starting ContactManager");
        try {
            loadContacts();
            started = true;
        } catch (DbException e) {
            throw new ServiceException("Failed to start ContactManager", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        LOG.info("Stopping ContactManager");
        started = false;
    }

    @Override
    public ContactId addContact(Author author, boolean verified) throws DbException, ContactExistsException {
        if (!started) {
            throw new IllegalStateException("ContactManager not started");
        }

        ContactId contactId = new ContactId(author.getId().getBytes());
        
        // Check if contact already exists
        if (contacts.containsKey(contactId)) {
            throw new ContactExistsException("Contact already exists: " + author.getName());
        }

        Contact contact = new Contact(contactId, author, verified);
        
        // Store in database
        Transaction txn = db.startTransaction(false);
        try {
            storeContact(txn, contact);
            db.commitTransaction(txn);
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }

        // Cache the contact
        contacts.put(contactId, contact);
        
        LOG.info("Added contact: " + author.getName());
        return contactId;
    }

    @Override
    public void removeContact(ContactId contactId) throws DbException {
        if (!started) {
            throw new IllegalStateException("ContactManager not started");
        }

        Contact contact = contacts.get(contactId);
        if (contact == null) {
            throw new DbException("Contact not found: " + contactId);
        }

        // Remove from database
        Transaction txn = db.startTransaction(false);
        try {
            deleteContact(txn, contactId);
            db.commitTransaction(txn);
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }

        // Remove from cache
        contacts.remove(contactId);
        
        LOG.info("Removed contact: " + contact.getAuthor().getName());
    }

    @Override
    public Contact getContact(ContactId contactId) throws DbException {
        Contact contact = contacts.get(contactId);
        if (contact == null) {
            throw new DbException("Contact not found: " + contactId);
        }
        return contact;
    }

    @Override
    public Collection<Contact> getContacts() {
        return contacts.values();
    }

    @Override
    public boolean contactExists(ContactId contactId) {
        return contacts.containsKey(contactId);
    }

    @Override
    public boolean contactExists(AuthorId authorId) {
        for (Contact contact : contacts.values()) {
            if (contact.getAuthor().getId().equals(authorId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setContactVerified(ContactId contactId, boolean verified) throws DbException {
        if (!started) {
            throw new IllegalStateException("ContactManager not started");
        }

        Contact contact = contacts.get(contactId);
        if (contact == null) {
            throw new DbException("Contact not found: " + contactId);
        }

        // Create updated contact
        Contact updatedContact = new Contact(contactId, contact.getAuthor(), verified);
        
        // Update in database
        Transaction txn = db.startTransaction(false);
        try {
            updateContact(txn, updatedContact);
            db.commitTransaction(txn);
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }

        // Update cache
        contacts.put(contactId, updatedContact);
        
        LOG.info("Updated contact verification: " + contact.getAuthor().getName() + " -> " + verified);
    }

    private void loadContacts() throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            Collection<Contact> loaded = loadContactsFromDb(txn);
            db.commitTransaction(txn);
            
            for (Contact contact : loaded) {
                contacts.put(contact.getId(), contact);
            }
            
            LOG.info("Loaded " + loaded.size() + " contacts");
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    private Collection<Contact> loadContactsFromDb(Transaction txn) throws DbException {
        // TODO: Implement database loading of contacts
        return contacts.values();
    }

    private void storeContact(Transaction txn, Contact contact) throws DbException {
        // TODO: Implement database storage of contacts
        LOG.info("Storing contact: " + contact.getAuthor().getName());
    }

    private void updateContact(Transaction txn, Contact contact) throws DbException {
        // TODO: Implement database update of contacts
        LOG.info("Updating contact: " + contact.getAuthor().getName());
    }

    private void deleteContact(Transaction txn, ContactId contactId) throws DbException {
        // TODO: Implement database deletion of contacts
        LOG.info("Deleting contact: " + contactId);
    }
}
