package org.nodex.core.identity;

import org.nodex.api.identity.IdentityManager;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.crypto.KeyPair;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.SyncConstants;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Implementation of IdentityManager that manages local and remote author identities.
 */
@ThreadSafe
@NotNullByDefault
public class IdentityManagerImpl implements IdentityManager, Service {

    private static final Logger LOG = Logger.getLogger(IdentityManagerImpl.class.getName());

    private final DatabaseComponent db;
    private final CryptoComponent crypto;
    private final AuthorFactory authorFactory;
    private final ConcurrentMap<AuthorId, LocalAuthor> localAuthors;
    private final ConcurrentMap<AuthorId, Author> authors;
    
    private volatile LocalAuthor localAuthor;
    private volatile boolean started = false;

    @Inject
    public IdentityManagerImpl(DatabaseComponent db, CryptoComponent crypto, AuthorFactory authorFactory) {
        this.db = db;
        this.crypto = crypto;
        this.authorFactory = authorFactory;
        this.localAuthors = new ConcurrentHashMap<>();
        this.authors = new ConcurrentHashMap<>();
    }

    @Override
    public void startService() throws ServiceException {
        LOG.info("Starting IdentityManager");
        try {
            // Load existing local authors from database
            loadLocalAuthors();
            started = true;
        } catch (DbException e) {
            throw new ServiceException("Failed to start IdentityManager", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        LOG.info("Stopping IdentityManager");
        started = false;
    }

    @Override
    public LocalAuthor createLocalAuthor(String name) throws DbException {
        if (!started) {
            throw new IllegalStateException("IdentityManager not started");
        }
        
        LocalAuthor author = authorFactory.createLocalAuthor(name);
        
        // Store in database
        Transaction txn = db.startTransaction(false);
        try {
            storeLocalAuthor(txn, author);
            db.commitTransaction(txn);
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
        
        // Cache the author
        localAuthors.put(author.getId(), author);
        
        // Set as default local author if none exists
        if (localAuthor == null) {
            localAuthor = author;
        }
        
        LOG.info("Created local author: " + name);
        return author;
    }

    @Override
    public void registerLocalAuthor(LocalAuthor author) throws DbException {
        if (!started) {
            throw new IllegalStateException("IdentityManager not started");
        }
        
        // Store in database
        Transaction txn = db.startTransaction(false);
        try {
            storeLocalAuthor(txn, author);
            db.commitTransaction(txn);
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
        
        // Cache the author
        localAuthors.put(author.getId(), author);
        
        // Set as default local author if none exists
        if (localAuthor == null) {
            localAuthor = author;
        }
        
        LOG.info("Registered local author: " + author.getName());
    }

    @Override
    public LocalAuthor getLocalAuthor() {
        return localAuthor;
    }

    @Override
    public Collection<LocalAuthor> getLocalAuthors() {
        return localAuthors.values();
    }

    @Override
    public void setLocalAuthor(LocalAuthor author) {
        if (!localAuthors.containsKey(author.getId())) {
            throw new IllegalArgumentException("Author not registered");
        }
        this.localAuthor = author;
        LOG.info("Set local author: " + author.getName());
    }

    @Override
    public Author getAuthor(AuthorId authorId) {
        return authors.get(authorId);
    }

    @Override
    public void addAuthor(Author author) {
        authors.put(author.getId(), author);
        LOG.info("Added author: " + author.getName());
    }

    @Override
    public Collection<Author> getAuthors() {
        return authors.values();
    }

    @Override
    public boolean hasAuthor(AuthorId authorId) {
        return authors.containsKey(authorId) || localAuthors.containsKey(authorId);
    }

    private void loadLocalAuthors() throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            Collection<LocalAuthor> loaded = loadLocalAuthors(txn);
            db.commitTransaction(txn);
            
            for (LocalAuthor author : loaded) {
                localAuthors.put(author.getId(), author);
                if (localAuthor == null) {
                    localAuthor = author;
                }
            }
            
            LOG.info("Loaded " + loaded.size() + " local authors");
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    private Collection<LocalAuthor> loadLocalAuthors(Transaction txn) throws DbException {
        // TODO: Implement database loading of local authors
        return localAuthors.values();
    }

    private void storeLocalAuthor(Transaction txn, LocalAuthor author) throws DbException {
        // TODO: Implement database storage of local authors
        // For now, just log the action
        LOG.info("Storing local author: " + author.getName());
    }
}
