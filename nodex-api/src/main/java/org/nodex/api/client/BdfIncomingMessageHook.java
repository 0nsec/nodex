package org.nodex.api.client;

import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.validation.IncomingMessageHook;
import org.nodex.api.sync.validation.MessageValidator;

import javax.annotation.concurrent.Immutable;

/**
 * A message hook that handles incoming BDF messages.
 */
@Immutable
@NotNullByDefault
public abstract class BdfIncomingMessageHook implements IncomingMessageHook {

    private final ClientId clientId;
    private final int majorVersion;

    protected BdfIncomingMessageHook(ClientId clientId, int majorVersion) {
        this.clientId = clientId;
        this.majorVersion = majorVersion;
    }

    @Override
    public DeliveryAction incomingMessage(Transaction txn, Message message, BdfDictionary metadata) throws DbException {
        try {
            BdfList messageList = parseMessage(message);
            return incomingMessage(txn, message, messageList, metadata);
        } catch (Exception e) {
            return DeliveryAction.REJECT;
        }
    }

    protected abstract DeliveryAction incomingMessage(Transaction txn, Message message, BdfList messageList, BdfDictionary metadata) throws DbException;

    protected abstract BdfList parseMessage(Message message) throws Exception;

    protected ClientId getClientId() {
        return clientId;
    }

    protected int getMajorVersion() {
        return majorVersion;
    }
}
