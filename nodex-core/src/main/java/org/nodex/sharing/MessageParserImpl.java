package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sharing.Shareable;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.sharing.MessageType.INVITE;
import static org.nodex.sharing.SharingConstants.MSG_KEY_AUTO_DELETE_TIMER;
import static org.nodex.sharing.SharingConstants.MSG_KEY_AVAILABLE_TO_ANSWER;
import static org.nodex.sharing.SharingConstants.MSG_KEY_INVITATION_ACCEPTED;
import static org.nodex.sharing.SharingConstants.MSG_KEY_IS_AUTO_DECLINE;
import static org.nodex.sharing.SharingConstants.MSG_KEY_LOCAL;
import static org.nodex.sharing.SharingConstants.MSG_KEY_MESSAGE_TYPE;
import static org.nodex.sharing.SharingConstants.MSG_KEY_READ;
import static org.nodex.sharing.SharingConstants.MSG_KEY_SHAREABLE_ID;
import static org.nodex.sharing.SharingConstants.MSG_KEY_TIMESTAMP;
import static org.nodex.sharing.SharingConstants.MSG_KEY_VISIBLE_IN_UI;
@Immutable
@NotNullByDefault
abstract class MessageParserImpl<S extends Shareable>
		implements MessageParser<S> {
	protected final ClientHelper clientHelper;
	MessageParserImpl(ClientHelper clientHelper) {
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
	public BdfDictionary getInvitesAvailableToAnswerQuery(GroupId shareableId) {
		return BdfDictionary.of(
				BdfEntry.of(MSG_KEY_AVAILABLE_TO_ANSWER, true),
				BdfEntry.of(MSG_KEY_MESSAGE_TYPE, INVITE.getValue()),
				BdfEntry.of(MSG_KEY_SHAREABLE_ID, shareableId)
		);
	}
	@Override
	public MessageMetadata parseMetadata(BdfDictionary meta)
			throws FormatException {
		MessageType type =
				MessageType.fromValue(meta.getInt(MSG_KEY_MESSAGE_TYPE));
		GroupId shareableId = new GroupId(meta.getRaw(MSG_KEY_SHAREABLE_ID));
		long timestamp = meta.getLong(MSG_KEY_TIMESTAMP);
		boolean local = meta.getBoolean(MSG_KEY_LOCAL);
		boolean read = meta.getBoolean(MSG_KEY_READ, false);
		boolean visible = meta.getBoolean(MSG_KEY_VISIBLE_IN_UI, false);
		boolean available = meta.getBoolean(MSG_KEY_AVAILABLE_TO_ANSWER, false);
		boolean accepted = meta.getBoolean(MSG_KEY_INVITATION_ACCEPTED, false);
		long timer = meta.getLong(MSG_KEY_AUTO_DELETE_TIMER,
				NO_AUTO_DELETE_TIMER);
		boolean isAutoDecline = meta.getBoolean(MSG_KEY_IS_AUTO_DECLINE, false);
		return new MessageMetadata(type, shareableId, timestamp, local, read,
				visible, available, accepted, timer, isAutoDecline);
	}
	@Override
	public InviteMessage<S> getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException {
		Message message = clientHelper.getMessage(txn, m);
		BdfList body = clientHelper.toList(message);
		return parseInviteMessage(message, body);
	}
	@Override
	public InviteMessage<S> parseInviteMessage(Message m, BdfList body)
			throws FormatException {
		byte[] b = body.getOptionalRaw(1);
		MessageId previousMessageId = (b == null ? null : new MessageId(b));
		BdfList descriptor = body.getList(2);
		S shareable = createShareable(descriptor);
		String text = body.getOptionalString(3);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 5) timer = body.getLong(4, NO_AUTO_DELETE_TIMER);
		return new InviteMessage<>(m.getId(), previousMessageId,
				m.getGroupId(), shareable, text, m.getTimestamp(), timer);
	}
	@Override
	public AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException {
		GroupId shareableId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = (b == null ? null : new MessageId(b));
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);
		return new AcceptMessage(m.getId(), previousMessageId, m.getGroupId(),
				shareableId, m.getTimestamp(), timer);
	}
	@Override
	public DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException {
		GroupId shareableId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = (b == null ? null : new MessageId(b));
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);
		return new DeclineMessage(m.getId(), m.getGroupId(), shareableId,
				m.getTimestamp(), previousMessageId, timer);
	}
	@Override
	public LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException {
		GroupId shareableId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = (b == null ? null : new MessageId(b));
		return new LeaveMessage(m.getId(), m.getGroupId(), shareableId,
				m.getTimestamp(), previousMessageId);
	}
	@Override
	public AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException {
		GroupId shareableId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = (b == null ? null : new MessageId(b));
		return new AbortMessage(m.getId(), m.getGroupId(), shareableId,
				m.getTimestamp(), previousMessageId);
	}
}
