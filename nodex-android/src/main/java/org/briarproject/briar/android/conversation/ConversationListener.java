package org.nodex.android.conversation;
import android.view.View;
import org.nodex.android.attachment.AttachmentItem;
import org.nodex.nullsafety.NotNullByDefault;
import androidx.annotation.UiThread;
@UiThread
@NotNullByDefault
interface ConversationListener {
	void respondToRequest(ConversationRequestItem item, boolean accept);
	void openRequestedShareable(ConversationRequestItem item);
	void onAttachmentClicked(View view, ConversationMessageItem messageItem,
			AttachmentItem attachmentItem);
	void onAutoDeleteTimerNoticeClicked();
	void onLinkClick(String url);
}