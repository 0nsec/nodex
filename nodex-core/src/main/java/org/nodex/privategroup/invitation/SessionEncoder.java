package org.nodex.privategroup.invitation;
import org.nodex.api.data.BdfDictionary;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionEncoder {
	BdfDictionary encodeSession(Session s);
}