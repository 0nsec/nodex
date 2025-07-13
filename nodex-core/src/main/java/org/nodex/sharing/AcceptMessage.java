package org.nodex.sharing;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
@Immutable
@NotNullByDefault
class AcceptMessage extends DeletableSharingMessage {
	AcceptMessage(MessageId id, @Nullable MessageId previousMessageId,
			GroupId contactGroupId, GroupId shareableId, long timestamp,
			long autoDeleteTimer) {
		super(id, contactGroupId, shareableId, timestamp, previousMessageId,
				autoDeleteTimer);
	}
}
