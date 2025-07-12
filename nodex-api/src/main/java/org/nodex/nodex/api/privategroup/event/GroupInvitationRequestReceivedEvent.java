package org.nodex.api.privategroup.event;
import org.nodex.core.api.contact.ContactId;
import org.nodex.api.conversation.event.ConversationMessageReceivedEvent;
import org.nodex.api.privategroup.invitation.GroupInvitationRequest;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
public class GroupInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<GroupInvitationRequest> {
	public GroupInvitationRequestReceivedEvent(GroupInvitationRequest request,
			ContactId contactId) {
		super(request, contactId);
	}
}