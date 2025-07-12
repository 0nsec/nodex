package org.nodex.sharing;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionParser {
	BdfDictionary getSessionQuery(SessionId s);
	BdfDictionary getAllSessionsQuery();
	boolean isSession(BdfDictionary d) throws FormatException;
	Session parseSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
}