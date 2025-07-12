package org.nodex.api.introduction;

import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.identity.Author;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Request to introduce two contacts to each other
 */
@Immutable
@NotNullByDefault
public class IntroductionRequest extends ConversationRequest<Author> {
    
    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitIntroductionRequest(this);
    }
}
