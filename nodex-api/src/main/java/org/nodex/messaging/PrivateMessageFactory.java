package org.nodex.api.messaging;
import org.nodex.api.FormatException;
import org.nodex.api.sync.GroupId;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.List;
import javax.annotation.Nullable;
@NotNullByDefault
public interface PrivateMessageFactory {
	PrivateMessage createLegacyPrivateMessage(GroupId groupId, long timestamp,
			String text) throws FormatException;
	PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers)
			throws FormatException;
	PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers,
			long autoDeleteTimer) throws FormatException;
}