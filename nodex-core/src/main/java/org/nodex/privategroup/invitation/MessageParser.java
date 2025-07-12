package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.nullsafety.NotNullByDefault;
@NotNullByDefault
interface MessageParser {
	BdfDictionary getMessagesVisibleInUiQuery();
	BdfDictionary getInvitesAvailableToAnswerQuery();
	BdfDictionary getInvitesAvailableToAnswerQuery(GroupId privateGroupId);
	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;
	InviteMessage getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException;
	InviteMessage parseInviteMessage(Message m, BdfList body)
			throws FormatException;
	JoinMessage parseJoinMessage(Message m, BdfList body)
			throws FormatException;
	LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException;
	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;
}