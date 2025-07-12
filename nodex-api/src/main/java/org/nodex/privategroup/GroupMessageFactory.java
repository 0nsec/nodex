package org.nodex.api.privategroup;
import org.nodex.api.crypto.CryptoExecutor;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
import static org.nodex.api.privategroup.PrivateGroupManager.CLIENT_ID;
@NotNullByDefault
public interface GroupMessageFactory {
	String SIGNING_LABEL_JOIN = CLIENT_ID.getString() + "/JOIN";
	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";
	@CryptoExecutor
	GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor creator);
	@CryptoExecutor
	GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, long inviteTimestamp, byte[] creatorSignature);
	@CryptoExecutor
	GroupMessage createGroupMessage(GroupId groupId, long timestamp,
			@Nullable MessageId parentId, LocalAuthor author, String text,
			MessageId previousMsgId);
}