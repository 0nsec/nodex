package org.nodex.privategroup;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.data.BdfList;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.Message;
import org.nodex.core.api.sync.MessageId;
import org.nodex.api.privategroup.GroupMessage;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.privategroup.MessageType.JOIN;
import static org.nodex.api.privategroup.MessageType.POST;
@Immutable
@NotNullByDefault
class GroupMessageFactoryImpl implements GroupMessageFactory {
	private final ClientHelper clientHelper;
	@Inject
	GroupMessageFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}
	@Override
	public GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor creator) {
		return createJoinMessage(groupId, timestamp, creator, null);
	}
	@Override
	public GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, long inviteTimestamp, byte[] creatorSignature) {
		BdfList invite = BdfList.of(inviteTimestamp, creatorSignature);
		return createJoinMessage(groupId, timestamp, member, invite);
	}
	private GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, @Nullable BdfList invite) {
		try {
			BdfList memberList = clientHelper.toList(member);
			BdfList toSign = BdfList.of(
					groupId,
					timestamp,
					memberList,
					invite
			);
			byte[] memberSignature = clientHelper.sign(SIGNING_LABEL_JOIN,
					toSign, member.getPrivateKey());
			BdfList body = BdfList.of(
					JOIN.getInt(),
					memberList,
					invite,
					memberSignature
			);
			Message m = clientHelper.createMessage(groupId, timestamp, body);
			return new GroupMessage(m, null, member);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
	@Override
	public GroupMessage createGroupMessage(GroupId groupId, long timestamp,
			@Nullable MessageId parentId, LocalAuthor member, String text,
			MessageId previousMsgId) {
		try {
			BdfList memberList = clientHelper.toList(member);
			BdfList toSign = BdfList.of(
					groupId,
					timestamp,
					memberList,
					parentId,
					previousMsgId,
					text
			);
			byte[] signature = clientHelper.sign(SIGNING_LABEL_POST, toSign,
					member.getPrivateKey());
			BdfList body = BdfList.of(
					POST.getInt(),
					memberList,
					parentId,
					previousMsgId,
					text,
					signature
			);
			Message m = clientHelper.createMessage(groupId, timestamp, body);
			return new GroupMessage(m, parentId, member);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
}