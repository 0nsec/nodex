package org.nodex.android.attachment;
import android.net.Uri;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.List;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
@UiThread
@NotNullByDefault
public interface AttachmentManager {
	LiveData<AttachmentResult> storeAttachments(Collection<Uri> uri,
			boolean restart);
	List<AttachmentHeader> getAttachmentHeadersForSending();
	void cancel();
}