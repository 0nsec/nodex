package org.nodex.api.blog;

import org.nodex.api.FormatException;
import org.nodex.api.data.BdfList;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.sync.validation.MessageValidator;

@NotNullByDefault
public interface BlogPostValidator extends MessageValidator {
    
    MessageContext validateMessage(Message message, GroupId groupId) throws FormatException;
    
    MessageContext validateMessage(Message message, GroupId groupId, BdfList body) throws FormatException;
}
