package org.nodex.sharing;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class DeclineMessage extends DeletableSharingMessage {
	DeclineMessage(MessageId id, GroupId contactGroupId,
			GroupId shareableId, long timestamp,
			@Nullable MessageId previousMessageId, long autoDeleteTimer) {
		super(id, contactGroupId, shareableId, timestamp, previousMessageId,
				autoDeleteTimer);
	}
}
