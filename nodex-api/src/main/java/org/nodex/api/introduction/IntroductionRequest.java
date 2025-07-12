package org.nodex.api.introduction;

import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.identity.Author;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Request to introduce two contacts to each other
 */
@Immutable
@NotNullByDefault
public class IntroductionRequest extends ConversationRequest<Author> {
    // Implementation details would go here
    // For now, this is a stub to resolve compilation errors
}
