package org.nodex.api.client;

import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.validation.MessageValidator;

import javax.annotation.concurrent.Immutable;

/**
 * A message validator that validates BDF messages.
 */
@Immutable
@NotNullByDefault
public abstract class BdfMessageValidator implements MessageValidator {

    private final ClientId clientId;
    private final int majorVersion;

    protected BdfMessageValidator(ClientId clientId, int majorVersion) {
        this.clientId = clientId;
        this.majorVersion = majorVersion;
    }

    @Override
    public MessageContext validateMessage(Message message, Group group) throws InvalidMessageException {
        try {
            BdfList messageList = parseMessage(message);
            return validateMessage(messageList, group);
        } catch (Exception e) {
            throw new InvalidMessageException("Invalid BDF message", e);
        }
    }

    protected abstract MessageContext validateMessage(BdfList messageList, Group group) throws InvalidMessageException;

    protected abstract BdfList parseMessage(Message message) throws Exception;

    protected ClientId getClientId() {
        return clientId;
    }

    protected int getMajorVersion() {
        return majorVersion;
    }
}
