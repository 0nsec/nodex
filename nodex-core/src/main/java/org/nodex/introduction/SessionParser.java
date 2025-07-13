package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.sync.GroupId;
import org.nodex.api.client.SessionId;
import org.nodex.api.introduction.Role;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionParser {
	BdfDictionary getSessionQuery(SessionId s);
	Role getRole(BdfDictionary d) throws FormatException;
	IntroducerSession parseIntroducerSession(BdfDictionary d)
			throws FormatException;
	IntroduceeSession parseIntroduceeSession(GroupId introducerGroupId,
			BdfDictionary d) throws FormatException;
}
