package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sharing.Shareable;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface MessageParser<S extends Shareable> {
	BdfDictionary getMessagesVisibleInUiQuery();
	BdfDictionary getInvitesAvailableToAnswerQuery();
	BdfDictionary getInvitesAvailableToAnswerQuery(GroupId shareableId);
	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;
	S createShareable(BdfList descriptor) throws FormatException;
	InviteMessage<S> getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException;
	InviteMessage<S> parseInviteMessage(Message m, BdfList body)
			throws FormatException;
	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;
	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;
	LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException;
	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;
}