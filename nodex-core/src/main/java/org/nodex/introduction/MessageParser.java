package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.Message;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface MessageParser {
	BdfDictionary getMessagesVisibleInUiQuery();
	BdfDictionary getRequestsAvailableToAnswerQuery(SessionId sessionId);
	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;
	RequestMessage parseRequestMessage(Message m, BdfList body)
			throws FormatException;
	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;
	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;
	AuthMessage parseAuthMessage(Message m, BdfList body)
			throws FormatException;
	ActivateMessage parseActivateMessage(Message m, BdfList body)
			throws FormatException;
	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;
}
