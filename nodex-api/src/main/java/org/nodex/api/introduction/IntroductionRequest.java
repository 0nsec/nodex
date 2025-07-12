package org.nodex.api.introduction;

import org.nodex.api.client.SessionId;
import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Request to introduce two contacts to each other
 */
@Immutable
@NotNullByDefault
public class IntroductionRequest extends ConversationRequest<Author> {
    
    public IntroductionRequest(MessageId messageId, GroupId groupId,
                              long timestamp, boolean local, boolean read,
                              boolean sent, boolean seen, SessionId sessionId,
                              Author author, @Nullable String text,
                              boolean answered, long autoDeleteTimer) {
        super(messageId, groupId, timestamp, local, read, sent, seen,
                sessionId, author, text, answered, autoDeleteTimer);
    }
    
    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitIntroductionRequest(this);
    }
}
