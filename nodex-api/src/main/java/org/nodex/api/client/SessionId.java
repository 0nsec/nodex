package org.nodex.api.client;
import org.nodex.api.UniqueId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.ThreadSafe;
@ThreadSafe
@NotNullByDefault
public class SessionId extends UniqueId {
	public SessionId(byte[] id) {
		super(id);
	}
}