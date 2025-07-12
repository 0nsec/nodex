package org.nodex.privategroup.invitation;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface SessionEncoder {
	BdfDictionary encodeSession(Session s);
}