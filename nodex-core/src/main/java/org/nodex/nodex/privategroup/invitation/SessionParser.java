package org.nodex.privategroup.invitation;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.core.api.sync.GroupId;
import org.nodex.api.client.SessionId;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionParser {
	BdfDictionary getSessionQuery(SessionId s);
	BdfDictionary getAllSessionsQuery();
	Role getRole(BdfDictionary d) throws FormatException;
	boolean isSession(BdfDictionary d) throws FormatException;
	Session parseSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
	CreatorSession parseCreatorSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
	InviteeSession parseInviteeSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
	PeerSession parsePeerSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
}