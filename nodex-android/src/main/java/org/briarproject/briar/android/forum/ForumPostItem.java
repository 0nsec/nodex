package org.nodex.android.forum;
import org.nodex.android.threaded.ThreadItem;
import org.nodex.api.forum.ForumPostHeader;
import javax.annotation.concurrent.NotThreadSafe;
@NotThreadSafe
class ForumPostItem extends ThreadItem {
	ForumPostItem(ForumPostHeader h, String text) {
		super(h.getId(), h.getParentId(), text, h.getTimestamp(), h.getAuthor(),
				h.getAuthorInfo(), h.isRead());
	}
}