package org.nodex.forum;
import org.nodex.api.FormatException;
import org.nodex.api.UniqueId;
import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.client.BdfMessageValidator;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.nodex.core.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static org.nodex.core.util.ValidationUtils.checkLength;
import static org.nodex.core.util.ValidationUtils.checkSize;
import static org.nodex.api.forum.ForumConstants.KEY_AUTHOR;
import static org.nodex.api.forum.ForumConstants.KEY_PARENT;
import static org.nodex.api.forum.ForumConstants.KEY_READ;
import static org.nodex.api.forum.ForumConstants.KEY_TIMESTAMP;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
import static org.nodex.api.forum.ForumPostFactory.SIGNING_LABEL_POST;
@Immutable
@NotNullByDefault
class ForumPostValidator extends BdfMessageValidator {
	ForumPostValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}
	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {
		checkSize(body, 4);
		byte[] parent = body.getOptionalRaw(0);
		checkLength(parent, UniqueId.LENGTH);
		BdfList authorList = body.getList(1);
		Author author = clientHelper.parseAndValidateAuthor(authorList);
		String text = body.getString(2);
		checkLength(text, 0, MAX_FORUM_POST_TEXT_LENGTH);
		byte[] sig = body.getRaw(3);
		checkLength(sig, 1, MAX_SIGNATURE_LENGTH);
		BdfList signed = BdfList.of(g.getId(), m.getTimestamp(), parent,
				authorList, text);
		try {
			clientHelper.verifySignature(sig, SIGNING_LABEL_POST,
					signed, author.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new InvalidMessageException(e);
		}
		BdfDictionary meta = new BdfDictionary();
		Collection<MessageId> dependencies = emptyList();
		meta.put(KEY_TIMESTAMP, m.getTimestamp());
		if (parent != null) {
			meta.put(KEY_PARENT, parent);
			dependencies = singletonList(new MessageId(parent));
		}
		meta.put(KEY_AUTHOR, authorList);
		meta.put(KEY_READ, false);
		return new BdfMessageContext(meta, dependencies);
	}
}