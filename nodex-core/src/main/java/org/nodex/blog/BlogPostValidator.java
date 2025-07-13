package org.nodex.blog;
import org.nodex.api.FormatException;
import org.nodex.api.client.BdfMessageContext;
import org.nodex.api.sync.BdfMessageContextImpl;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.MessageId;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.sync.validation.MessageValidator;
import org.nodex.api.system.Clock;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.MessageType;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;
import static java.util.Collections.singletonList;
import static org.nodex.core.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static org.nodex.core.util.ValidationUtils.checkLength;
import static org.nodex.core.util.ValidationUtils.checkSize;
import static org.nodex.api.blog.BlogConstants.KEY_AUTHOR;
import static org.nodex.api.blog.BlogConstants.KEY_COMMENT;
import static org.nodex.api.blog.BlogConstants.KEY_ORIGINAL_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_ORIGINAL_PARENT_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_PARENT_MSG_ID;
import static org.nodex.api.blog.BlogConstants.KEY_READ;
import static org.nodex.api.blog.BlogConstants.KEY_RSS_FEED;
import static org.nodex.api.blog.BlogConstants.KEY_TIMESTAMP;
import static org.nodex.api.blog.BlogConstants.KEY_TIME_RECEIVED;
import static org.nodex.api.blog.BlogConstants.KEY_TYPE;
import static org.nodex.api.blog.BlogConstants.MAX_BLOG_COMMENT_TEXT_LENGTH;
import static org.nodex.api.blog.BlogConstants.MAX_BLOG_POST_TEXT_LENGTH;
import static org.nodex.api.blog.BlogManager.CLIENT_ID;
import static org.nodex.api.blog.BlogManager.MAJOR_VERSION;
import static org.nodex.api.blog.BlogPostFactory.SIGNING_LABEL_COMMENT;
import static org.nodex.api.blog.BlogPostFactory.SIGNING_LABEL_POST;
import static org.nodex.api.blog.MessageType.COMMENT;
import static org.nodex.api.blog.MessageType.POST;
@Immutable
@NotNullByDefault
public class BlogPostValidator implements MessageValidator {
	private final GroupFactory groupFactory;
	private final MessageFactory messageFactory;
	private final BlogFactory blogFactory;
	private final ClientHelper clientHelper;
	private final Clock clock;
	
	public BlogPostValidator(GroupFactory groupFactory, MessageFactory messageFactory,
			BlogFactory blogFactory, ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		this.groupFactory = groupFactory;
		this.messageFactory = messageFactory;
		this.blogFactory = blogFactory;
		this.clientHelper = clientHelper;
		this.clock = clock;
	}
	
	@Override
	public MessageContext validateMessage(Message m, Group g) throws InvalidMessageException {
		try {
			BdfList body = clientHelper.getMessageAsList(null, m.getId());
			BdfMessageContext c;
			int type = body.getInt(0);
			body.remove(0);
			switch (MessageType.valueOf(type)) {
				case POST:
					c = validatePost(m, g, body);
					addMessageMetadata(c, m.getTimestamp());
					break;
				case COMMENT:
					c = validateComment(m, g, body);
					addMessageMetadata(c, m.getTimestamp());
					break;
				case WRAPPED_POST:
					c = validateWrappedPost(body);
					break;
				case WRAPPED_COMMENT:
					c = validateWrappedComment(body);
					break;
				default:
					throw new InvalidMessageException("Unknown Message Type");
			}
			c.getDictionary().put(KEY_TYPE, type);
			return c;
		} catch (Exception e) {
			throw new InvalidMessageException("Failed to validate message", e);
		}
	}
	
	private BdfMessageContext validatePost(Message m, Group g, BdfList body)
			throws InvalidMessageException, FormatException {
		checkSize(body, 2);
		String text = body.getString(0);
		checkLength(text, 0, MAX_BLOG_POST_TEXT_LENGTH);
		byte[] sig = body.getRaw(1);
		checkLength(sig, 1, MAX_SIGNATURE_LENGTH);
		BdfList signed = BdfList.of(g.getId(), m.getTimestamp(), text);
		Blog b = blogFactory.parseBlog(g);
		Author a = b.getAuthor();
		try {
			clientHelper.verifySignature(sig, SIGNING_LABEL_POST, signed,
					a.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new InvalidMessageException(e);
		}
		BdfDictionary meta = new BdfDictionary();
		meta.put(KEY_ORIGINAL_MSG_ID, m.getId());
		meta.put(KEY_AUTHOR, clientHelper.toList(a));
		meta.put(KEY_RSS_FEED, b.isRssFeed());
		return new BdfMessageContextImpl(m, body, meta, m.getTimestamp());
	}
	private BdfMessageContext validateComment(Message m, Group g, BdfList body)
			throws InvalidMessageException, FormatException {
		checkSize(body, 4);
		String comment = body.getOptionalString(0);
		checkLength(comment, 1, MAX_BLOG_COMMENT_TEXT_LENGTH);
		byte[] pOriginalIdBytes = body.getRaw(1);
		checkLength(pOriginalIdBytes, MessageId.LENGTH);
		MessageId pOriginalId = new MessageId(pOriginalIdBytes);
		byte[] currentIdBytes = body.getRaw(2);
		checkLength(currentIdBytes, MessageId.LENGTH);
		MessageId currentId = new MessageId(currentIdBytes);
		byte[] sig = body.getRaw(3);
		checkLength(sig, 1, MAX_SIGNATURE_LENGTH);
		BdfList signed = BdfList.of(g.getId(), m.getTimestamp(), comment,
				pOriginalId, currentId);
		Blog b = blogFactory.parseBlog(g);
		Author a = b.getAuthor();
		try {
			clientHelper.verifySignature(sig, SIGNING_LABEL_COMMENT,
					signed, a.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new InvalidMessageException(e);
		}
		BdfDictionary meta = new BdfDictionary();
		if (comment != null) meta.put(KEY_COMMENT, comment);
		meta.put(KEY_ORIGINAL_MSG_ID, m.getId());
		meta.put(KEY_ORIGINAL_PARENT_MSG_ID, pOriginalId);
		meta.put(KEY_PARENT_MSG_ID, currentId);
		meta.put(KEY_AUTHOR, clientHelper.toList(a));
		Collection<MessageId> dependencies = singletonList(currentId);
		return new BdfMessageContext(meta, dependencies);
	}
	private BdfMessageContext validateWrappedPost(BdfList body)
			throws InvalidMessageException, FormatException {
		checkSize(body, 4);
		byte[] descriptor = body.getRaw(0);
		long wTimestamp = body.getLong(1);
		if (wTimestamp < 0) throw new FormatException();
		String text = body.getString(2);
		checkLength(text, 0, MAX_BLOG_POST_TEXT_LENGTH);
		byte[] signature = body.getRaw(3);
		checkLength(signature, 1, MAX_SIGNATURE_LENGTH);
		Group wGroup = groupFactory.createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
				descriptor);
		Blog wBlog = blogFactory.parseBlog(wGroup);
		BdfList wBodyList = BdfList.of(POST.getInt(), text, signature);
		byte[] wBody = clientHelper.toByteArray(wBodyList);
		Message wMessage =
				messageFactory.createMessage(wGroup.getId(), wTimestamp, wBody);
		wBodyList.remove(0);
		BdfMessageContext c = validatePost(wMessage, wGroup, wBodyList);
		BdfDictionary meta = new BdfDictionary();
		meta.put(KEY_ORIGINAL_MSG_ID, wMessage.getId());
		meta.put(KEY_TIMESTAMP, wTimestamp);
		meta.put(KEY_AUTHOR, c.getDictionary().getList(KEY_AUTHOR));
		meta.put(KEY_RSS_FEED, wBlog.isRssFeed());
		return new BdfMessageContext(meta);
	}
	private BdfMessageContext validateWrappedComment(BdfList body)
			throws InvalidMessageException, FormatException {
		checkSize(body, 7);
		byte[] descriptor = body.getRaw(0);
		long wTimestamp = body.getLong(1);
		if (wTimestamp < 0) throw new FormatException();
		String comment = body.getOptionalString(2);
		checkLength(comment, 1, MAX_BLOG_COMMENT_TEXT_LENGTH);
		byte[] pOriginalIdBytes = body.getRaw(3);
		checkLength(pOriginalIdBytes, MessageId.LENGTH);
		MessageId pOriginalId = new MessageId(pOriginalIdBytes);
		byte[] oldIdBytes = body.getRaw(4);
		checkLength(oldIdBytes, MessageId.LENGTH);
		MessageId oldId = new MessageId(oldIdBytes);
		byte[] signature = body.getRaw(5);
		checkLength(signature, 1, MAX_SIGNATURE_LENGTH);
		byte[] parentIdBytes = body.getRaw(6);
		checkLength(parentIdBytes, MessageId.LENGTH);
		MessageId parentId = new MessageId(parentIdBytes);
		Group wGroup = groupFactory.createGroup(CLIENT_ID.toString(), MAJOR_VERSION,
				descriptor);
		BdfList wBodyList = BdfList.of(COMMENT.getInt(), comment, pOriginalId,
				oldId, signature);
		byte[] wBody = clientHelper.toByteArray(wBodyList);
		Message wMessage =
				messageFactory.createMessage(wGroup.getId(), wTimestamp, wBody);
		wBodyList.remove(0);
		BdfMessageContext c = validateComment(wMessage, wGroup, wBodyList);
		Collection<MessageId> dependencies = singletonList(parentId);
		BdfDictionary meta = new BdfDictionary();
		meta.put(KEY_ORIGINAL_MSG_ID, wMessage.getId());
		meta.put(KEY_ORIGINAL_PARENT_MSG_ID, pOriginalId);
		meta.put(KEY_PARENT_MSG_ID, parentId);
		meta.put(KEY_TIMESTAMP, wTimestamp);
		if (comment != null) meta.put(KEY_COMMENT, comment);
		meta.put(KEY_AUTHOR, c.getDictionary().getList(KEY_AUTHOR));
		return new BdfMessageContext(meta, dependencies);
	}
	private void addMessageMetadata(BdfMessageContext c, long time) {
		c.getDictionary().put(KEY_TIMESTAMP, time);
		c.getDictionary().put(KEY_TIME_RECEIVED, clock.currentTimeMillis());
		c.getDictionary().put(KEY_READ, false);
	}
}
