package org.nodex.avatar;
import org.nodex.api.FormatException;
import org.nodex.api.Pair;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.system.Clock;
import org.nodex.api.attachment.FileTooBigException;
import org.nodex.api.avatar.AvatarMessageEncoder;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static org.nodex.core.util.IoUtils.copyAndClose;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static org.nodex.avatar.AvatarConstants.MSG_KEY_VERSION;
import static org.nodex.avatar.AvatarConstants.MSG_TYPE_UPDATE;
@Immutable
@NotNullByDefault
class AvatarMessageEncoderImpl implements AvatarMessageEncoder {
	private final ClientHelper clientHelper;
	private final Clock clock;
	@Inject
	AvatarMessageEncoderImpl(ClientHelper clientHelper, Clock clock) {
		this.clientHelper = clientHelper;
		this.clock = clock;
	}
	@Override
	public Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException {
		BdfList list = BdfList.of(MSG_TYPE_UPDATE, version, contentType);
		byte[] descriptor = clientHelper.toByteArray(list);
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		bodyOut.write(descriptor);
		copyAndClose(in, bodyOut);
		if (bodyOut.size() > MAX_MESSAGE_BODY_LENGTH)
			throw new FileTooBigException();
		byte[] body = bodyOut.toByteArray();
		long timestamp = clock.currentTimeMillis();
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_CONTENT_TYPE, contentType);
		meta.put(MSG_KEY_DESCRIPTOR_LENGTH, descriptor.length);
		return new Pair<>(m, meta);
	}
}
