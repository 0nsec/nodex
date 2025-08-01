package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupFactory;
import org.nodex.api.nullsafety.NotNullByDefault;
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
import static org.nodex.privategroup.invitation.MessageType.INVITE;
@Immutable
@NotNullByDefault
class MessageParserImpl implements MessageParser {
	private final PrivateGroupFactory privateGroupFactory;
	private final ClientHelper clientHelper;
	@Inject
	MessageParserImpl(PrivateGroupFactory privateGroupFactory,
			ClientHelper clientHelper) {
		this.privateGroupFactory = privateGroupFactory;
		this.clientHelper = clientHelper;
	}
	@Override
	public BdfDictionary getMessagesVisibleInUiQuery() {
		return BdfDictionary.of(BdfEntry.of(MSG_KEY_VISIBLE_IN_UI, true));
	}
	@Override
	public BdfDictionary getInvitesAvailableToAnswerQuery() {
		return BdfDictionary.of(
				BdfEntry.of(MSG_KEY_AVAILABLE_TO_ANSWER, true),
				BdfEntry.of(MSG_KEY_MESSAGE_TYPE, INVITE.getValue())
		);
	}
	@Override
	public BdfDictionary getInvitesAvailableToAnswerQuery(
			GroupId privateGroupId) {
		return BdfDictionary.of(
				BdfEntry.of(MSG_KEY_AVAILABLE_TO_ANSWER, true),
				BdfEntry.of(MSG_KEY_MESSAGE_TYPE, INVITE.getValue()),
				BdfEntry.of(MSG_KEY_PRIVATE_GROUP_ID, privateGroupId)
		);
	}
	@Override
	public MessageMetadata parseMetadata(BdfDictionary meta)
			throws FormatException {
		MessageType type =
				MessageType.fromValue(meta.getInt(MSG_KEY_MESSAGE_TYPE));
		GroupId privateGroupId =
				new GroupId(meta.getRaw(MSG_KEY_PRIVATE_GROUP_ID));
		long timestamp = meta.getLong(MSG_KEY_TIMESTAMP);
		boolean local = meta.getBoolean(MSG_KEY_LOCAL);
		boolean read = meta.getBoolean(MSG_KEY_READ, false);
		boolean visible = meta.getBoolean(MSG_KEY_VISIBLE_IN_UI, false);
		boolean available = meta.getBoolean(MSG_KEY_AVAILABLE_TO_ANSWER, false);
		boolean accepted = meta.getBoolean(MSG_KEY_INVITATION_ACCEPTED, false);
		long timer = meta.getLong(MSG_KEY_AUTO_DELETE_TIMER,
				NO_AUTO_DELETE_TIMER);
		boolean isAutoDecline = meta.getBoolean(MSG_KEY_IS_AUTO_DECLINE, false);
		return new MessageMetadata(type, privateGroupId, timestamp, local, read,
				visible, available, accepted, timer, isAutoDecline);
	}
	@Override
	public InviteMessage getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException {
		Message message = clientHelper.getMessage(txn, m);
		BdfList body = clientHelper.toList(message);
		return parseInviteMessage(message, body);
	}
	@Override
	public InviteMessage parseInviteMessage(Message m, BdfList body)
			throws FormatException {
		BdfList creatorList = body.getList(1);
		String groupName = body.getString(2);
		byte[] salt = body.getRaw(3);
		String text = body.getOptionalString(4);
		byte[] signature = body.getRaw(5);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 7) timer = body.getLong(6, NO_AUTO_DELETE_TIMER);
		Author creator = clientHelper.parseAndValidateAuthor(creatorList);
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				groupName, creator, salt);
		return new InviteMessage(m.getId(), m.getGroupId(),
				privateGroup.getId(), m.getTimestamp(), groupName, creator,
				salt, text, signature, timer);
	}
	@Override
	public JoinMessage parseJoinMessage(Message m, BdfList body)
			throws FormatException {
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = b == null ? null : new MessageId(b);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);
		return new JoinMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp(), previousMessageId, timer);
	}
	@Override
	public LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException {
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = b == null ? null : new MessageId(b);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);
		return new LeaveMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp(), previousMessageId, timer);
	}
	@Override
	public AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException {
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		return new AbortMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp());
	}
}
