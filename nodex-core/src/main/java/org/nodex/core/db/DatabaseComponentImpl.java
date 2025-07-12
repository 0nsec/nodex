package org.nodex.core.db;

import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DatabaseConfig;
import org.nodex.api.db.Transaction;
import org.nodex.api.db.DbException;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    private void initializeSchema() throws SQLException {
        // TODO: Initialize database schema
        // For now, just create basic tables
        LOG.info("Initializing database schema");
        
        // Create settings table
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS settings (" +
            "key VARCHAR(255) PRIMARY KEY, " +
            "value VARCHAR(1024)" +
            ")"
        );
        
        // Create contacts table
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS contacts (" +
            "id BINARY(32) PRIMARY KEY, " +
            "name VARCHAR(255) NOT NULL, " +
            "public_key BINARY(512) NOT NULL, " +
            "created_time BIGINT NOT NULL" +
            ")"
        );
        
        // Create messages table
        connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS messages (" +
            "id BINARY(32) PRIMARY KEY, " +
            "contact_id BINARY(32), " +
            "content TEXT, " +
            "timestamp BIGINT NOT NULL, " +
            "FOREIGN KEY (contact_id) REFERENCES contacts(id)" +
            ")"
        );
        
        LOG.info("Database schema initialized");
    }
}
