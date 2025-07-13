package org.nodex.api.sync.validation;

import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Message;

@NotNullByDefault
public interface IncomingMessageHookFixed {
    
    enum DeliveryAction {
        ACCEPT_DO_NOT_SHARE,
        ACCEPT_SHARE,
        REJECT
    }
    
    DeliveryAction incomingMessage(Transaction txn, Message message) throws DbException;
}
