package org.nodex.forum;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.forum.ForumPost;
import org.nodex.api.forum.ForumPostFactory;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.core.util.StringUtils.utf8IsTooLong;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
@Immutable
@NotNullByDefault
class ForumPostFactoryImpl implements ForumPostFactory {
	private final ClientHelper clientHelper;
	@Inject
	ForumPostFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}
	@Override
	public ForumPost createPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException {
		if (utf8IsTooLong(text, MAX_FORUM_POST_TEXT_LENGTH))
			throw new IllegalArgumentException();
		BdfList authorList = clientHelper.toList(author);
		BdfList signed = BdfList.of(groupId, timestamp, parent, authorList,
				text);
		byte[] sig = clientHelper.sign(SIGNING_LABEL_POST, signed,
				author.getPrivateKey());
		BdfList message = BdfList.of(parent, authorList, text, sig);
		Message m = clientHelper.createMessage(groupId, timestamp, message);
		return new ForumPost(m, parent, author);
	}
}
