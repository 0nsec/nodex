package org.nodex.api.avatar;
import org.nodex.core.api.Pair;
import org.nodex.core.api.data.BdfDictionary;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import java.io.IOException;
import java.io.InputStream;
public interface AvatarMessageEncoder {
	Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException;
}