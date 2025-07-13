package org.nodex.messaging;
import org.nodex.api.FormatException;
import org.nodex.api.UniqueId;
import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.sync.BdfMessageContextImpl;
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
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.messaging.MessagingConstants.MAX_ATTACHMENTS_PER_MESSAGE;
import static org.nodex.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static org.nodex.client.MessageTrackerConstants.MSG_KEY_READ;
import static org.nodex.messaging.MessageTypes.ATTACHMENT;
import static org.nodex.messaging.MessageTypes.PRIVATE_MESSAGE;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_ATTACHMENT_HEADERS;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_AUTO_DELETE_TIMER;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_HAS_TEXT;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_LOCAL;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_MSG_TYPE;
import static org.nodex.messaging.MessagingConstants.MSG_KEY_TIMESTAMP;
import static org.nodex.util.ValidationUtils.validateAutoDeleteTimer;
@Immutable
@NotNullByDefault
class PrivateMessageValidator implements MessageValidator {
	private final BdfReaderFactory bdfReaderFactory;
	private final MetadataEncoder metadataEncoder;
	private final Clock clock;
	PrivateMessageValidator(BdfReaderFactory bdfReaderFactory,
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
			BdfList list = reader.readBdfList();
			long bytesRead = countIn.getBytesRead();
		   MessageContext context;
		   if (list.size() == 1) {
			   if (!reader.eof()) throw new FormatException();
			   context = validateLegacyPrivateMessage(m, list);
		   } else {
			   int messageType = list.getInt(0);
			   if (messageType == PRIVATE_MESSAGE) {
				   if (!reader.eof()) throw new FormatException();
				   context = validatePrivateMessage(m, list);
			   } else if (messageType == ATTACHMENT) {
					context = validateAttachment(m, list, bytesRead);
				} else {
					throw new InvalidMessageException();
				}
			}
		   byte[] meta = metadataEncoder.encode(((BdfMessageContextImpl)context).getDictionary());
		   return new BdfMessageContextImpl(m, list, ((BdfMessageContextImpl)context).getDictionary(), m.getTimestamp());
		} catch (IOException e) {
			throw new InvalidMessageException(e);
		}
	}
   private MessageContext validateLegacyPrivateMessage(Message m,
			BdfList body) throws FormatException {
		checkSize(body, 1);
		String text = body.getString(0);
		checkLength(text, 0, MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_TIMESTAMP, m.getTimestamp());
		meta.put(MSG_KEY_LOCAL, false);
		meta.put(MSG_KEY_READ, false);
   return new BdfMessageContextImpl(m, body, meta, m.getTimestamp());
	}
   private MessageContext validatePrivateMessage(Message m, BdfList body)
			throws FormatException {
		checkSize(body, 3, 4);
		String text = body.getOptionalString(1);
		checkLength(text, 0, MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		BdfList headers = body.getList(2);
		if (text == null) checkSize(headers, 1, MAX_ATTACHMENTS_PER_MESSAGE);
		else checkSize(headers, 0, MAX_ATTACHMENTS_PER_MESSAGE);
		for (int i = 0; i < headers.size(); i++) {
			BdfList header = headers.getList(i);
			checkSize(header, 2);
			byte[] id = header.getRaw(0);
			checkLength(id, UniqueId.LENGTH);
			String contentType = header.getString(1);
			checkLength(contentType, 1, MAX_CONTENT_TYPE_BYTES);
		}
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(3));
		}
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_TIMESTAMP, m.getTimestamp());
		meta.put(MSG_KEY_LOCAL, false);
		meta.put(MSG_KEY_READ, false);
		meta.put(MSG_KEY_MSG_TYPE, PRIVATE_MESSAGE);
		meta.put(MSG_KEY_HAS_TEXT, text != null);
		meta.put(MSG_KEY_ATTACHMENT_HEADERS, headers);
		if (timer != NO_AUTO_DELETE_TIMER) {
			meta.put(MSG_KEY_AUTO_DELETE_TIMER, timer);
		}
		   return new BdfMessageContextImpl(m, null, meta, m.getTimestamp());
	}
   private MessageContext validateAttachment(Message m, BdfList descriptor,
			long descriptorLength) throws FormatException {
		checkSize(descriptor, 2);
		String contentType = descriptor.getString(1);
		checkLength(contentType, 1, MAX_CONTENT_TYPE_BYTES);
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_TIMESTAMP, m.getTimestamp());
		meta.put(MSG_KEY_LOCAL, false);
		meta.put(MSG_KEY_MSG_TYPE, ATTACHMENT);
		meta.put(MSG_KEY_DESCRIPTOR_LENGTH, descriptorLength);
		meta.put(MSG_KEY_CONTENT_TYPE, contentType);
		   return new BdfMessageContextImpl(m, null, meta, m.getTimestamp());
	}
}