package org.nodex.sharing;
import org.nodex.api.contact.ContactId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.conversation.ConversationRequest;
import org.nodex.api.sharing.InvitationResponse;
import org.nodex.api.sharing.Shareable;
public interface InvitationFactory<S extends Shareable, R extends InvitationResponse> {
	ConversationRequest<S> createInvitationRequest(boolean local, boolean sent,
			boolean seen, boolean read, InviteMessage<S> m, ContactId c,
			boolean available, boolean canBeOpened, long autoDeleteTimer);
	R createInvitationResponse(MessageId id, GroupId contactGroupId, long time,
			boolean local, boolean sent, boolean seen, boolean read,
			boolean accept, GroupId shareableId, long autoDeleteTimer,
			boolean isAutoDecline);
}
