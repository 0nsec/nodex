package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.client.MessageTrackerConstants.MSG_KEY_READ;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_AUTO_DELETE_TIMER;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_AVAILABLE_TO_ANSWER;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_INVITATION_ACCEPTED;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_IS_AUTO_DECLINE;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_LOCAL;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_MESSAGE_TYPE;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_PRIVATE_GROUP_ID;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_TIMESTAMP;
import static org.nodex.privategroup.invitation.GroupInvitationConstants.MSG_KEY_VISIBLE_IN_UI;
import static org.nodex.privategroup.invitation.MessageType.ABORT;
import static org.nodex.privategroup.invitation.MessageType.INVITE;
import static org.nodex.privategroup.invitation.MessageType.JOIN;
import static org.nodex.privategroup.invitation.MessageType.LEAVE;
@Immutable
@NotNullByDefault
class MessageEncoderImpl implements MessageEncoder {
	private final ClientHelper clientHelper;
	private final MessageFactory messageFactory;
	@Inject
	MessageEncoderImpl(ClientHelper clientHelper,
			MessageFactory messageFactory) {
		this.clientHelper = clientHelper;
		this.messageFactory = messageFactory;
	}
	@Override
	public BdfDictionary encodeMetadata(MessageType type,
			GroupId privateGroupId, long timestamp, boolean local, boolean read,
			boolean visible, boolean available, boolean accepted,
			long autoDeleteTimer, boolean isAutoDecline) {
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_MESSAGE_TYPE, type.getValue());
		meta.put(MSG_KEY_PRIVATE_GROUP_ID, privateGroupId);
		meta.put(MSG_KEY_TIMESTAMP, timestamp);
		meta.put(MSG_KEY_LOCAL, local);
		meta.put(MSG_KEY_READ, read);
		meta.put(MSG_KEY_VISIBLE_IN_UI, visible);
		meta.put(MSG_KEY_AVAILABLE_TO_ANSWER, available);
		meta.put(MSG_KEY_INVITATION_ACCEPTED, accepted);
		if (autoDeleteTimer != NO_AUTO_DELETE_TIMER) {
			meta.put(MSG_KEY_AUTO_DELETE_TIMER, autoDeleteTimer);
		}
		if (isAutoDecline) {
			meta.put(MSG_KEY_IS_AUTO_DECLINE, isAutoDecline);
		}
		return meta;
	}
	@Override
	public BdfDictionary encodeMetadata(MessageType type,
			GroupId privateGroupId, long timestamp, long autoDeleteTimer) {
		return encodeMetadata(type, privateGroupId, timestamp, false, false,
				false, false, false, autoDeleteTimer, false);
	}
	@Override
	public void setVisibleInUi(BdfDictionary meta, boolean visible) {
		meta.put(MSG_KEY_VISIBLE_IN_UI, visible);
	}
	@Override
	public void setAvailableToAnswer(BdfDictionary meta, boolean available) {
		meta.put(MSG_KEY_AVAILABLE_TO_ANSWER, available);
	}
	@Override
	public void setInvitationAccepted(BdfDictionary meta, boolean accepted) {
		meta.put(MSG_KEY_INVITATION_ACCEPTED, accepted);
	}
	@Override
	public Message encodeInviteMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, String groupName,
			Author creator, byte[] salt, @Nullable String text,
			byte[] signature) {
		BdfList creatorList = clientHelper.toList(creator);
		BdfList body = BdfList.of(
				INVITE.getValue(),
				creatorList,
				groupName,
				salt,
				text,
				signature
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeInviteMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp, String groupName,
			Author creator, byte[] salt, @Nullable String text,
			byte[] signature, long autoDeleteTimer) {
		BdfList creatorList = clientHelper.toList(creator);
		BdfList body = BdfList.of(
				INVITE.getValue(),
				creatorList,
				groupName,
				salt,
				text,
				signature,
				encodeTimer(autoDeleteTimer)
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeJoinMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId) {
		BdfList body = BdfList.of(
				JOIN.getValue(),
				privateGroupId,
				previousMessageId
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeJoinMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId, long autoDeleteTimer) {
		BdfList body = BdfList.of(
				JOIN.getValue(),
				privateGroupId,
				previousMessageId,
				encodeTimer(autoDeleteTimer)
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeLeaveMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId) {
		BdfList body = BdfList.of(
				LEAVE.getValue(),
				privateGroupId,
				previousMessageId
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeLeaveMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp,
			@Nullable MessageId previousMessageId, long autoDeleteTimer) {
		BdfList body = BdfList.of(
				LEAVE.getValue(),
				privateGroupId,
				previousMessageId,
				encodeTimer(autoDeleteTimer)
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	@Override
	public Message encodeAbortMessage(GroupId contactGroupId,
			GroupId privateGroupId, long timestamp) {
		BdfList body = BdfList.of(
				ABORT.getValue(),
				privateGroupId
		);
		return createMessage(contactGroupId, timestamp, body);
	}
	private Message createMessage(GroupId contactGroupId, long timestamp,
			BdfList body) {
		try {
			return messageFactory.createMessage(contactGroupId, timestamp,
					clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	@Nullable
	private Long encodeTimer(long autoDeleteTimer) {
		return autoDeleteTimer == NO_AUTO_DELETE_TIMER ? null : autoDeleteTimer;
	}
}
