package org.nodex.privategroup.invitation;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.core.api.data.BdfList;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
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