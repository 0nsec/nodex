package org.nodex.introduction;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.identity.Author;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionEncoder {
	BdfDictionary getIntroduceeSessionsByIntroducerQuery(Author introducer);
	BdfDictionary getIntroducerSessionsQuery();
	BdfDictionary encodeIntroducerSession(IntroducerSession s);
	BdfDictionary encodeIntroduceeSession(IntroduceeSession s);
}