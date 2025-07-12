package org.nodex.api.introduction;

import org.nodex.api.conversation.ConversationMessageVisitor;
import org.nodex.api.conversation.ConversationResponse;
import org.nodex.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Response to an introduction request
 */
@Immutable
@NotNullByDefault
public class IntroductionResponse extends ConversationResponse {
    
    @Override
    public <T> T accept(ConversationMessageVisitor<T> v) {
        return v.visitIntroductionResponse(this);
    }
}
