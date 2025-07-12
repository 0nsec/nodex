package org.nodex.sharing;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
interface MessageEncoder {
	BdfDictionary encodeMetadata(MessageType type, GroupId shareableId,
			long timestamp, boolean local, boolean read, boolean visible,
			boolean available, boolean accepted, long autoDeleteTimer,
			boolean isAutoDecline);
	void setVisibleInUi(BdfDictionary meta, boolean visible);
	void setAvailableToAnswer(BdfDictionary meta, boolean available);
	void setInvitationAccepted(BdfDictionary meta, boolean accepted);
	Message encodeInviteMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, BdfList descriptor,
			@Nullable String text);
	Message encodeInviteMessage(GroupId contactGroupId, long timestamp,
			@Nullable MessageId previousMessageId, BdfList descriptor,
			@Nullable String text, long autoDeleteTimer);
	Message encodeAcceptMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId);
	Message encodeAcceptMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer);
	Message encodeDeclineMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId);
	Message encodeDeclineMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer);
	Message encodeLeaveMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId);
	Message encodeAbortMessage(GroupId contactGroupId, GroupId shareableId,
			long timestamp, @Nullable MessageId previousMessageId);
}