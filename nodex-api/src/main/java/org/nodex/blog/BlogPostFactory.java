package org.nodex.api.blog;
import org.nodex.api.FormatException;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
@NotNullByDefault
public interface BlogPostFactory {
	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";
	String SIGNING_LABEL_COMMENT = CLIENT_ID.getString() + "/COMMENT";
	BlogPost createBlogPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException;
	Message createBlogComment(GroupId groupId, LocalAuthor author,
			@Nullable String comment, MessageId parentOriginalId,
			MessageId parentCurrentId)
			throws FormatException, GeneralSecurityException;
	Message wrapPost(GroupId groupId, byte[] descriptor, long timestamp,
			BdfList body) throws FormatException;
	Message rewrapWrappedPost(GroupId groupId, BdfList body)
			throws FormatException;
	Message wrapComment(GroupId groupId, byte[] descriptor, long timestamp,
			BdfList body, MessageId parentCurrentId) throws FormatException;
	Message rewrapWrappedComment(GroupId groupId, BdfList body,
			MessageId parentCurrentId) throws FormatException;
}