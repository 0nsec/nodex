package org.nodex.android.attachment;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.attachment.Attachment;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.messaging.PrivateMessageHeader;
import org.nodex.api.messaging.event.AttachmentReceivedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.io.InputStream;
import java.util.List;
import androidx.lifecycle.LiveData;
@NotNullByDefault
public interface AttachmentRetriever {
	@DatabaseExecutor
	Attachment getMessageAttachment(AttachmentHeader h) throws DbException;
	List<LiveData<AttachmentItem>> getAttachmentItems(
			PrivateMessageHeader messageHeader);
	@DatabaseExecutor
	void cacheAttachmentItemWithSize(MessageId conversationMessageId,
			AttachmentHeader h) throws DbException;
	AttachmentItem createAttachmentItem(Attachment a, boolean needsSize);
	@DatabaseExecutor
	void loadAttachmentItem(MessageId attachmentId);
}