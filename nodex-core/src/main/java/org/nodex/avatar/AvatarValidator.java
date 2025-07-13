package org.nodex.avatar;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfReader;
import org.nodex.api.data.BdfReaderFactory;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.db.Metadata;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.sync.MessageContextImpl;
import org.nodex.api.sync.validation.MessageValidator;
import org.nodex.api.system.Clock;
import org.nodex.attachment.CountingInputStream;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.concurrent.Immutable;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static org.nodex.core.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;
import static org.nodex.core.util.ValidationUtils.checkLength;
import static org.nodex.core.util.ValidationUtils.checkSize;
import static org.nodex.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static org.nodex.avatar.AvatarConstants.MSG_KEY_VERSION;
import static org.nodex.avatar.AvatarConstants.MSG_TYPE_UPDATE;
@Immutable
@NotNullByDefault
class AvatarValidator implements MessageValidator {
	private final BdfReaderFactory bdfReaderFactory;
	private final MetadataEncoder metadataEncoder;
	private final Clock clock;
	AvatarValidator(BdfReaderFactory bdfReaderFactory,
			MetadataEncoder metadataEncoder, Clock clock) {
		this.bdfReaderFactory = bdfReaderFactory;
		this.metadataEncoder = metadataEncoder;
		this.clock = clock;
	}
	@Override
	public MessageContext validateMessage(Message m, Group g)
			throws InvalidMessageException {
		long now = clock.currentTimeMillis();
		if (m.getTimestamp() - now > MAX_CLOCK_DIFFERENCE) {
			throw new InvalidMessageException(
					"Timestamp is too far in the future");
		}
		try {
			InputStream in = new ByteArrayInputStream(m.getBody());
			CountingInputStream countIn =
					new CountingInputStream(in, MAX_MESSAGE_BODY_LENGTH);
			BdfReader reader = bdfReaderFactory.createReader(countIn);
			BdfList list = reader.readList();
			long bytesRead = countIn.getBytesRead();
			BdfDictionary d = validateUpdate(list, bytesRead);
			byte[] meta = metadataEncoder.encode(d);
			return new MessageContextImpl(new Metadata(meta));
		} catch (IOException e) {
			throw new InvalidMessageException(e);
		}
	}
	private BdfDictionary validateUpdate(BdfList body, long descriptorLength)
			throws FormatException {
		checkSize(body, 3);
		int messageType = body.getInt(0);
		if (messageType != MSG_TYPE_UPDATE) throw new FormatException();
		long version = body.getLong(1);
		if (version < 0) throw new FormatException();
		String contentType = body.getString(2);
		checkLength(contentType, 1, MAX_CONTENT_TYPE_BYTES);
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_CONTENT_TYPE, contentType);
		meta.put(MSG_KEY_DESCRIPTOR_LENGTH, descriptorLength);
		return meta;
	}
}
