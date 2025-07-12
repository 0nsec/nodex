package org.nodex.api.client;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Message;

/**
 * Context for BDF (Binary Data Format) messages.
 */
@NotNullByDefault
public interface BdfMessageContext {
    
    /**
     * Returns the message being processed.
     */
    Message getMessage();
    
    /**
     * Returns the BDF list containing the message data.
     */
    org.nodex.api.data.BdfList getBdfList();
    
    /**
     * Returns the dictionary containing message metadata.
     */
    org.nodex.api.data.BdfDictionary getDictionary();
    
    /**
     * Returns the timestamp when the message was processed.
     */
    long getTimestamp();
}
