package org.nodex.api.sync.validation;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageContext;

/**
 * Interface for validating messages.
 */
@NotNullByDefault
public interface MessageValidator {
    
    /**
     * Validates a message and returns its context.
     * 
     * @param message The message to validate
     * @param group The group the message belongs to
     * @return The message context
     * @throws InvalidMessageException if the message is invalid
     */
    MessageContext validateMessage(Message message, Group group) throws InvalidMessageException;
}
