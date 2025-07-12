package org.nodex.api.introduction;

import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Response to an introduction request
 */
@Immutable
@NotNullByDefault
public class IntroductionResponse extends ConversationResponse {
    
    public IntroductionResponse(MessageId messageId, GroupId groupId,
                               long timestamp, boolean local, boolean read,
                               boolean sent, boolean seen, SessionId sessionId,
                               boolean accepted, long autoDeleteTimer,
                               boolean isAutoDecline) {
        super(messageId, groupId, timestamp, local, read, sent, seen,
                sessionId, accepted, autoDeleteTimer, isAutoDecline);
    }
    
    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitIntroductionResponse(this);
    }
}
