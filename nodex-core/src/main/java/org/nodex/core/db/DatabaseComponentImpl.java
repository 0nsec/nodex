package org.nodex.core.db;

import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DatabaseConfig;
import org.nodex.api.db.Transaction;
import org.nodex.api.db.TransactionManager;
import org.nodex.api.db.DbException;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Group;
import org.nodex.api.identity.Identity;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.transport.TransportKeys;
import org.nodex.api.settings.Settings;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Implementation of DatabaseComponent using H2 embedded database.
 */
@ThreadSafe
@NotNullByDefault
public class DatabaseComponentImpl implements DatabaseComponent, Service {

    private static final Logger LOG = Logger.getLogger(DatabaseComponentImpl.class.getName());

    private final DatabaseConfig config;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicLong transactionIdCounter = new AtomicLong(0);
    
    private Connection connection;

    @Inject
    public DatabaseComponentImpl(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public void startService() throws ServiceException {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Database already started");
        }
        
        try {
            LOG.info("Starting database: " + config.getDatabasePath());
            
            // Load H2 driver
            Class.forName("org.h2.Driver");
            
            // Connect to database
            String url = "jdbc:h2:" + config.getDatabasePath() + ";AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
            connection = DriverManager.getConnection(url, config.getUsername(), config.getPassword());
            
            // Initialize database schema
            initializeSchema();
            
            LOG.info("Database started successfully");
        } catch (Exception e) {
            started.set(false);
            throw new ServiceException("Failed to start database", e);
        }
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started.compareAndSet(true, false)) {
            return; // Already stopped
        }
        
        try {
            LOG.info("Stopping database");
            
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            LOG.info("Database stopped successfully");
        } catch (SQLException e) {
            throw new ServiceException("Failed to stop database", e);
        }
    }

    @Override
    public Transaction startTransaction(boolean readOnly) throws DbException {
        if (!started.get()) {
            throw new IllegalStateException("Database not started");
        }
        
        try {
            long transactionId = transactionIdCounter.incrementAndGet();
            Connection txnConnection = DriverManager.getConnection(
                "jdbc:h2:" + config.getDatabasePath() + ";AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
                config.getUsername(), 
                config.getPassword()
            );
            
            txnConnection.setAutoCommit(false);
            txnConnection.setReadOnly(readOnly);
            
            return new TransactionImpl(transactionId, txnConnection);
        } catch (SQLException e) {
            throw new DbException("Failed to start transaction", e);
        }
    }

    @Override
    public void commitTransaction(Transaction transaction) throws DbException {
        if (!(transaction instanceof TransactionImpl)) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        
        TransactionImpl txn = (TransactionImpl) transaction;
        try {
            txn.getConnection().commit();
            txn.getConnection().close();
        } catch (SQLException e) {
            throw new DbException("Failed to commit transaction", e);
        }
    }

    @Override
    public void abortTransaction(Transaction transaction) throws DbException {
        if (!(transaction instanceof TransactionImpl)) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        
        TransactionImpl txn = (TransactionImpl) transaction;
        try {
            txn.getConnection().rollback();
            txn.getConnection().close();
        } catch (SQLException e) {
            throw new DbException("Failed to abort transaction", e);
        }
    }

    @Override
    public void close() throws DbException {
        try {
            stopService();
        } catch (ServiceException e) {
            throw new DbException("Failed to close database", e);
        }
    }

    // Contact management methods
    @Override
    public void addContact(Transaction txn, Contact contact) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT INTO contacts (id, name, public_key, created_time) VALUES (?, ?, ?, ?)"
            );
            stmt.setBytes(1, contact.getId().getBytes());
            stmt.setString(2, contact.getAuthor().getName());
            stmt.setBytes(3, contact.getAuthor().getPublicKey());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to add contact", e);
        }
    }

    @Override
    public Contact getContact(Transaction txn, ContactId contactId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, name, public_key, created_time FROM contacts WHERE id = ?"
            );
            stmt.setBytes(1, contactId.getBytes());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Create Author and Contact from result set
                AuthorId authorId = new AuthorId(rs.getBytes("id"));
                String name = rs.getString("name");
                byte[] publicKey = rs.getBytes("public_key");
                Author author = new Author(authorId, name, publicKey);
                return new Contact(contactId, author, true);
            } else {
                throw new DbException("Contact not found");
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get contact", e);
        }
    }

    @Override
    public Collection<Contact> getContacts(Transaction txn) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        Collection<Contact> contacts = new ArrayList<>();
        
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, name, public_key, created_time FROM contacts"
            );
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ContactId contactId = new ContactId(rs.getBytes("id"));
                AuthorId authorId = new AuthorId(rs.getBytes("id"));
                String name = rs.getString("name");
                byte[] publicKey = rs.getBytes("public_key");
                Author author = new Author(authorId, name, publicKey);
                contacts.add(new Contact(contactId, author, true));
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get contacts", e);
        }
        
        return contacts;
    }

    @Override
    public void removeContact(Transaction txn, ContactId contactId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "DELETE FROM contacts WHERE id = ?"
            );
            stmt.setBytes(1, contactId.getBytes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to remove contact", e);
        }
    }

    // Message management methods
    @Override
    public void addMessage(Transaction txn, Message message, boolean shared) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT INTO messages (id, group_id, timestamp, body, shared) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setBytes(1, message.getId().getBytes());
            stmt.setBytes(2, message.getGroupId().getBytes());
            stmt.setLong(3, message.getTimestamp());
            stmt.setBytes(4, message.getBody());
            stmt.setBoolean(5, shared);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to add message", e);
        }
    }

    @Override
    public Message getMessage(Transaction txn, MessageId messageId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, group_id, timestamp, body FROM messages WHERE id = ?"
            );
            stmt.setBytes(1, messageId.getBytes());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                GroupId groupId = new GroupId(rs.getBytes("group_id"));
                long timestamp = rs.getLong("timestamp");
                byte[] body = rs.getBytes("body");
                return new Message(messageId, groupId, timestamp, body);
            } else {
                throw new DbException("Message not found");
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get message", e);
        }
    }

    @Override
    public Collection<Message> getMessages(Transaction txn, GroupId groupId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        Collection<Message> messages = new ArrayList<>();
        
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, group_id, timestamp, body FROM messages WHERE group_id = ?"
            );
            stmt.setBytes(1, groupId.getBytes());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MessageId messageId = new MessageId(rs.getBytes("id"));
                long timestamp = rs.getLong("timestamp");
                byte[] body = rs.getBytes("body");
                messages.add(new Message(messageId, groupId, timestamp, body));
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get messages", e);
        }
        
        return messages;
    }

    // Group management methods
    @Override
    public void addGroup(Transaction txn, Group group) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT INTO groups (id, CLIENT_ID.toString(), MAJOR_VERSION, descriptor) VALUES (?, ?, ?, ?)"
            );
            stmt.setBytes(1, group.getId().getBytes());
            stmt.setString(2, group.getClientId().getString());
            stmt.setInt(3, group.getMajorVersion());
            stmt.setBytes(4, group.getDescriptor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to add group", e);
        }
    }

    @Override
    public Group getGroup(Transaction txn, GroupId groupId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, CLIENT_ID.toString(), MAJOR_VERSION, descriptor FROM groups WHERE id = ?"
            );
            stmt.setBytes(1, groupId.getBytes());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Create Group from result set - simplified for now
                throw new DbException("Group retrieval not fully implemented");
            } else {
                throw new DbException("Group not found");
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get group", e);
        }
    }

    @Override
    public Collection<Group> getGroups(Transaction txn) throws DbException {
        // Implementation would retrieve all groups from database
        return new ArrayList<>();
    }

    // Identity management methods
    @Override
    public void setLocalIdentity(Transaction txn, Identity identity) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            // Store local identity in settings table
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)"
            );
            stmt.setString(1, "local_identity");
            stmt.setString(2, identity.getLocalAuthor().getName()); // Simplified
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to set local identity", e);
        }
    }

    @Override
    public Identity getLocalIdentity(Transaction txn) throws DbException {
        // Implementation would retrieve local identity from database
        throw new DbException("Local identity retrieval not implemented");
    }

    // Author management methods
    @Override
    public void addAuthor(Transaction txn, Author author) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT INTO authors (id, name, public_key) VALUES (?, ?, ?)"
            );
            stmt.setBytes(1, author.getId().getBytes());
            stmt.setString(2, author.getName());
            stmt.setBytes(3, author.getPublicKey());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to add author", e);
        }
    }

    @Override
    public Author getAuthor(Transaction txn, AuthorId authorId) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT id, name, public_key FROM authors WHERE id = ?"
            );
            stmt.setBytes(1, authorId.getBytes());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("name");
                byte[] publicKey = rs.getBytes("public_key");
                return new Author(authorId, name, publicKey);
            } else {
                throw new DbException("Author not found");
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get author", e);
        }
    }

    // Transport key management methods
    @Override
    public void addTransportKeys(Transaction txn, TransportId transportId, TransportKeys keys) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "INSERT INTO transport_keys (transport_id, keys_data) VALUES (?, ?)"
            );
            stmt.setString(1, transportId.getString());
            stmt.setBytes(2, keys.getEncoded()); // Simplified serialization
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DbException("Failed to add transport keys", e);
        }
    }

    @Override
    public TransportKeys getTransportKeys(Transaction txn, TransportId transportId) throws DbException {
        // Implementation would retrieve transport keys from database
        throw new DbException("Transport keys retrieval not implemented");
    }

    // Settings management methods
    @Override
    public Settings getSettings(Transaction txn, String namespace) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        Settings settings = new Settings();
        
        try {
            PreparedStatement stmt = transaction.getConnection().prepareStatement(
                "SELECT key, value FROM settings WHERE key LIKE ?"
            );
            stmt.setString(1, namespace + ".%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String key = rs.getString("key").substring(namespace.length() + 1);
                String value = rs.getString("value");
                settings.put(key, value);
            }
        } catch (SQLException e) {
            throw new DbException("Failed to get settings", e);
        }
        
        return settings;
    }

    @Override
    public void mergeSettings(Transaction txn, Settings settings, String namespace) throws DbException {
        TransactionImpl transaction = (TransactionImpl) txn;
        try {
            for (String key : settings.keySet()) {
                PreparedStatement stmt = transaction.getConnection().prepareStatement(
                    "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)"
                );
                stmt.setString(1, namespace + "." + key);
                stmt.setString(2, settings.get(key));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DbException("Failed to merge settings", e);
        }
    }
}
