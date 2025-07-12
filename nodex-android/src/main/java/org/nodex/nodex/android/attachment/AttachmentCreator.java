package org.nodex.android.attachment;
import android.net.Uri;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
@NotNullByDefault
public interface AttachmentCreator {
	@UiThread
	LiveData<AttachmentResult> storeAttachments(LiveData<GroupId> groupId,
			Collection<Uri> newUris);
	@UiThread
	LiveData<AttachmentResult> getLiveAttachments();
	@UiThread
	List<AttachmentHeader> getAttachmentHeadersForSending();
	@UiThread
	void onAttachmentsSent(MessageId id);
	@UiThread
	void cancel();
	@UiThread
	void deleteUnsentAttachments();
	@IoExecutor
	void onAttachmentHeaderReceived(Uri uri, AttachmentHeader h,
			boolean needsSize);
	@IoExecutor
	void onAttachmentError(Uri uri, Throwable t);
	@IoExecutor
	void onAttachmentCreationFinished();
}