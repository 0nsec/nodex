package org.nodex.privategroup.invitation;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
interface MessageEncoder {
	BdfDictionary encodeMetadata(MessageType type, GroupId privateGroupId,
			long timestamp, boolean local, boolean read, boolean visible,
			boolean available, boolean accepted, long autoDeleteTimer,
			boolean isAutoDecline);
	BdfDictionary encodeMetadata(MessageType type, GroupId privateGroupId,
			long timestamp, long autoDeleteTimer);
	void setVisibleInUi(BdfDictionary meta, boolean visible);
	void setAvailableToAnswer(BdfDictionary meta, boolean available);
	void setInvitationAccepted(BdfDictionary meta, boolean accepted);
	Message encodeInviteMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, String groupName, Author creator, byte[] salt,
			@Nullable String text, byte[] signature);
	Message encodeInviteMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, String groupName, Author creator, byte[] salt,
			@Nullable String text, byte[] signature, long autoDeleteTimer);
	Message encodeJoinMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId);
	Message encodeJoinMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer);
	Message encodeLeaveMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId);
	Message encodeLeaveMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp, @Nullable MessageId previousMessageId,
			long autoDeleteTimer);
	Message encodeAbortMessage(GroupId contactGroupId, GroupId privateGroupId,
			long timestamp);
}
