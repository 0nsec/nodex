package org.nodex.api.forum;
import org.nodex.api.FormatException;
import org.nodex.api.crypto.CryptoExecutor;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;
import static org.nodex.api.forum.ForumManager.CLIENT_ID;
@NotNullByDefault
public interface ForumPostFactory {
	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";
	@CryptoExecutor
	ForumPost createPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException;
}