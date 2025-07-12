package org.nodex.core.validation;

import org.nodex.api.sync.validation.ValidationManager;
import org.nodex.api.sync.validation.MessageValidator;
import org.nodex.api.sync.validation.IncomingMessageHook;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Implementation of ValidationManager that coordinates message validation.
 */
@ThreadSafe
@NotNullByDefault
public class ValidationManagerImpl implements ValidationManager, Service {

    private static final Logger LOG = Logger.getLogger(ValidationManagerImpl.class.getName());

    private final ConcurrentMap<ClientId, MessageValidator> validators;
    private final ConcurrentMap<ClientId, IncomingMessageHook> hooks;
    private volatile boolean started = false;

    @Inject
    public ValidationManagerImpl() {
        this.validators = new ConcurrentHashMap<>();
        this.hooks = new ConcurrentHashMap<>();
    }

    @Override
    public void startService() throws ServiceException {
        LOG.info("Starting ValidationManager");
        started = true;
    }

    @Override
    public void stopService() throws ServiceException {
        LOG.info("Stopping ValidationManager");
        started = false;
    }

    @Override
    public void registerMessageValidator(ClientId clientId, MessageValidator validator) {
        if (!started) {
            throw new IllegalStateException("ValidationManager not started");
        }
        validators.put(clientId, validator);
        LOG.info("Registered message validator for client: " + clientId);
    }

    @Override
    public void registerIncomingMessageHook(ClientId clientId, IncomingMessageHook hook) {
        if (!started) {
            throw new IllegalStateException("ValidationManager not started");
        }
        hooks.put(clientId, hook);
        LOG.info("Registered incoming message hook for client: " + clientId);
    }

    @Override
    public MessageContext validateMessage(Message message, Group group) throws InvalidMessageException {
        if (!started) {
            throw new IllegalStateException("ValidationManager not started");
        }

        ClientId clientId = group.getClientId();
        MessageValidator validator = validators.get(clientId);
        
        if (validator == null) {
            throw new InvalidMessageException("No validator registered for client: " + clientId);
        }

        return validator.validateMessage(message, group);
    }

    @Override
    public IncomingMessageHook.DeliveryAction processIncomingMessage(Transaction txn, Message message, 
                                                                    Group group) throws DbException {
        if (!started) {
            throw new IllegalStateException("ValidationManager not started");
        }

        ClientId clientId = group.getClientId();
        IncomingMessageHook hook = hooks.get(clientId);
        
        if (hook == null) {
            // Default action if no hook is registered
            return IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
        }

        // Create empty metadata for now - this should be populated from the message
        return hook.incomingMessage(txn, message, new org.nodex.api.data.BdfDictionary());
    }

    @Override
    public boolean hasValidator(ClientId clientId) {
        return validators.containsKey(clientId);
    }

    @Override
    public boolean hasIncomingMessageHook(ClientId clientId) {
        return hooks.containsKey(clientId);
    }
}
