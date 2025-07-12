package org.nodex.api.avatar;
import org.nodex.api.Pair;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import java.io.IOException;
import java.io.InputStream;
public interface AvatarMessageEncoder {
	Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException;
}