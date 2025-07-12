package org.nodex.messaging;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.data.BdfList;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.core.util.StringUtils.utf8IsTooLong;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static org.nodex.messaging.MessageTypes.PRIVATE_MESSAGE;
@Immutable
@NotNullByDefault
class PrivateMessageFactoryImpl implements PrivateMessageFactory {
	private final ClientHelper clientHelper;
	@Inject
	PrivateMessageFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}
	@Override
	public PrivateMessage createLegacyPrivateMessage(GroupId groupId,
			long timestamp, String text) throws FormatException {
		if (utf8IsTooLong(text, MAX_PRIVATE_MESSAGE_TEXT_LENGTH))
			throw new IllegalArgumentException();
		BdfList body = BdfList.of(text);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m);
	}
	@Override
	public PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers)
			throws FormatException {
		validateTextAndAttachmentHeaders(text, headers);
		BdfList attachmentList = serialiseAttachmentHeaders(headers);
		BdfList body = BdfList.of(PRIVATE_MESSAGE, text, attachmentList);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m, text != null, headers);
	}
	@Override
	public PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers,
			long autoDeleteTimer) throws FormatException {
		validateTextAndAttachmentHeaders(text, headers);
		BdfList attachmentList = serialiseAttachmentHeaders(headers);
		Long timer = autoDeleteTimer == NO_AUTO_DELETE_TIMER ?
				null : autoDeleteTimer;
		BdfList body = BdfList.of(PRIVATE_MESSAGE, text, attachmentList, timer);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m, text != null, headers, autoDeleteTimer);
	}
	private void validateTextAndAttachmentHeaders(@Nullable String text,
			List<AttachmentHeader> headers) {
		if (text == null) {
			if (headers.isEmpty()) throw new IllegalArgumentException();
		} else if (utf8IsTooLong(text, MAX_PRIVATE_MESSAGE_TEXT_LENGTH)) {
			throw new IllegalArgumentException();
		}
	}
	private BdfList serialiseAttachmentHeaders(List<AttachmentHeader> headers) {
		BdfList attachmentList = new BdfList();
		for (AttachmentHeader a : headers) {
			attachmentList.add(
					BdfList.of(a.getMessageId(), a.getContentType()));
		}
		return attachmentList;
	}
}