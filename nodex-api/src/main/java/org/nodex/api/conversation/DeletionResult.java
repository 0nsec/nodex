package org.nodex.api.conversation;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
import static org.nodex.api.conversation.ConversationManager.DELETE_SESSION_INTRODUCTION_INCOMPLETE;
import static org.nodex.api.conversation.ConversationManager.DELETE_SESSION_INTRODUCTION_IN_PROGRESS;
import static org.nodex.api.conversation.ConversationManager.DELETE_SESSION_INVITATION_INCOMPLETE;
import static org.nodex.api.conversation.ConversationManager.DELETE_SESSION_INVITATION_IN_PROGRESS;
@NotThreadSafe
@NotNullByDefault
public class DeletionResult {
	private int result = 0;
	public void addDeletionResult(DeletionResult deletionResult) {
		result |= deletionResult.result;
	}
	public void addInvitationNotAllSelected() {
		result |= DELETE_SESSION_INVITATION_INCOMPLETE;
	}
	public void addInvitationSessionInProgress() {
		result |= DELETE_SESSION_INVITATION_IN_PROGRESS;
	}
	public void addIntroductionNotAllSelected() {
		result |= DELETE_SESSION_INTRODUCTION_INCOMPLETE;
	}
	public void addIntroductionSessionInProgress() {
		result |= DELETE_SESSION_INTRODUCTION_IN_PROGRESS;
	}
	public boolean allDeleted() {
		return result == 0;
	}
	public boolean hasIntroductionSessionInProgress() {
		return (result & DELETE_SESSION_INTRODUCTION_IN_PROGRESS) != 0;
	}
	public boolean hasInvitationSessionInProgress() {
		return (result & DELETE_SESSION_INVITATION_IN_PROGRESS) != 0;
	}
	public boolean hasNotAllIntroductionSelected() {
		return (result & DELETE_SESSION_INTRODUCTION_INCOMPLETE) != 0;
	}
	public boolean hasNotAllInvitationSelected() {
		return (result & DELETE_SESSION_INVITATION_INCOMPLETE) != 0;
	}
}