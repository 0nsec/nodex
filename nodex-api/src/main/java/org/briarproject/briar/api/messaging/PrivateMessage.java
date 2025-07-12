package org.nodex.api.messaging;
import org.nodex.core.api.sync.Message;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import static java.util.Collections.emptyList;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_IMAGES;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_IMAGES_AUTO_DELETE;
import static org.nodex.api.messaging.PrivateMessageFormat.TEXT_ONLY;
@Immutable
@NotNullByDefault
public class PrivateMessage {
	private final Message message;
	private final boolean hasText;
	private final List<AttachmentHeader> attachmentHeaders;
	private final long autoDeleteTimer;
	private final PrivateMessageFormat format;
	public PrivateMessage(Message message) {
		this.message = message;
		hasText = true;
		attachmentHeaders = emptyList();
		autoDeleteTimer = NO_AUTO_DELETE_TIMER;
		format = TEXT_ONLY;
	}
	public PrivateMessage(Message message, boolean hasText,
			List<AttachmentHeader> headers) {
		this.message = message;
		this.hasText = hasText;
		this.attachmentHeaders = headers;
		autoDeleteTimer = NO_AUTO_DELETE_TIMER;
		format = TEXT_IMAGES;
	}
	public PrivateMessage(Message message, boolean hasText,
			List<AttachmentHeader> headers, long autoDeleteTimer) {
		this.message = message;
		this.hasText = hasText;
		this.attachmentHeaders = headers;
		this.autoDeleteTimer = autoDeleteTimer;
		format = TEXT_IMAGES_AUTO_DELETE;
	}
	public Message getMessage() {
		return message;
	}
	public PrivateMessageFormat getFormat() {
		return format;
	}
	public boolean hasText() {
		return hasText;
	}
	public List<AttachmentHeader> getAttachmentHeaders() {
		return attachmentHeaders;
	}
	public long getAutoDeleteTimer() {
		return autoDeleteTimer;
	}
}