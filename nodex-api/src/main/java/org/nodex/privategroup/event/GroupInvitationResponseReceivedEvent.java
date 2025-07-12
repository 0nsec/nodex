package org.nodex.api.privategroup.event;
import org.nodex.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.privategroup.invitation.GroupInvitationResponse;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupInvitationResponseReceivedEvent
		extends ConversationMessageReceivedEvent<GroupInvitationResponse> {
	public GroupInvitationResponseReceivedEvent(
			GroupInvitationResponse response, ContactId contactId) {
		super(response, contactId);
	}
}