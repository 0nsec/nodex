package org.nodex.core.sync;

import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Implementation of MessageFactory that creates Message instances.
 */
@Immutable
@NotNullByDefault
public class MessageFactoryImpl implements MessageFactory {

    private final CryptoComponent crypto;

    @Inject
    public MessageFactoryImpl(CryptoComponent crypto) {
        this.crypto = crypto;
    }

    @Override
    public Message createMessage(GroupId groupId, long timestamp, byte[] body) {
        MessageId messageId = generateMessageId(groupId, timestamp, body);
        return new Message(messageId, groupId, timestamp, body);
    }

    @Override
    public Message createMessage(GroupId groupId, long timestamp, byte[] body, MessageId... dependencies) {
        MessageId messageId = generateMessageId(groupId, timestamp, body, dependencies);
        return new Message(messageId, groupId, timestamp, body);
    }

    private MessageId generateMessageId(GroupId groupId, long timestamp, byte[] body, MessageId... dependencies) {
        // Create a unique message ID by hashing the group ID, timestamp, body, and dependencies
        byte[] input = new byte[groupId.getBytes().length + 8 + body.length];
        
        // Copy group ID
        System.arraycopy(groupId.getBytes(), 0, input, 0, groupId.getBytes().length);
        
        // Copy timestamp (8 bytes)
        int offset = groupId.getBytes().length;
        for (int i = 0; i < 8; i++) {
            input[offset + i] = (byte) (timestamp >>> (8 * (7 - i)));
        }
        
        // Copy body
        offset += 8;
        System.arraycopy(body, 0, input, offset, body.length);
        
        // Include dependencies in the hash
        if (dependencies.length > 0) {
            byte[] withDeps = Arrays.copyOf(input, input.length + dependencies.length * 32);
            offset = input.length;
            for (MessageId dep : dependencies) {
                System.arraycopy(dep.getBytes(), 0, withDeps, offset, 32);
                offset += 32;
            }
            input = withDeps;
        }
        
        // Hash to create message ID
        byte[] hash = crypto.hash(input);
        return new MessageId(hash);
    }
}
