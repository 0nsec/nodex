package org.nodex.messaging;
import org.nodex.api.UniqueId;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.crypto.CryptoComponent;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.MessageId;
import org.nodex.core.test.BrambleTestCase;
import org.nodex.api.attachment.AttachmentHeader;
import org.nodex.api.forum.ForumPost;
import org.nodex.api.forum.ForumPostFactory;
import org.nodex.api.messaging.PrivateMessage;
import org.nodex.api.messaging.PrivateMessageFactory;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import static org.nodex.core.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.nodex.core.api.identity.AuthorConstants.MAX_PUBLIC_KEY_LENGTH;
import static org.nodex.core.api.record.Record.MAX_RECORD_PAYLOAD_BYTES;
import static org.nodex.core.test.TestUtils.getRandomBytes;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.util.IoUtils.copyAndClose;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static org.nodex.api.attachment.MediaConstants.MAX_IMAGE_SIZE;
import static org.nodex.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
import static org.nodex.api.messaging.MessagingConstants.MAX_ATTACHMENTS_PER_MESSAGE;
import static org.nodex.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static org.nodex.messaging.MessageTypes.ATTACHMENT;
import static org.junit.Assert.assertTrue;
public class MessageSizeIntegrationTest extends BrambleTestCase {
	@Inject
	CryptoComponent crypto;
	@Inject
	AuthorFactory authorFactory;
	@Inject
	PrivateMessageFactory privateMessageFactory;
	@Inject
	ForumPostFactory forumPostFactory;
	@Inject
	ClientHelper clientHelper;
	@Inject
	MessageFactory messageFactory;
	public MessageSizeIntegrationTest() {
		MessageSizeIntegrationTestComponent component =
				DaggerMessageSizeIntegrationTestComponent.builder().build();
		MessageSizeIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
	}
	@Test
	public void testLegacyPrivateMessageFitsIntoRecord() throws Exception {
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		String text = getRandomString(MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		PrivateMessage message = privateMessageFactory
				.createLegacyPrivateMessage(groupId, timestamp, text);
		int length = message.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}
	@Test
	public void testPrivateMessageFitsIntoRecord() throws Exception {
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		String text = getRandomString(MAX_PRIVATE_MESSAGE_TEXT_LENGTH);
		List<AttachmentHeader> headers = new ArrayList<>();
		for (int i = 0; i < MAX_ATTACHMENTS_PER_MESSAGE; i++) {
			headers.add(new AttachmentHeader(groupId,
					new MessageId(getRandomId()),
					getRandomString(MAX_CONTENT_TYPE_BYTES)));
		}
		PrivateMessage message = privateMessageFactory.createPrivateMessage(
				groupId, timestamp, text, headers, MAX_AUTO_DELETE_TIMER_MS);
		int length = message.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ MAX_PRIVATE_MESSAGE_TEXT_LENGTH + MAX_ATTACHMENTS_PER_MESSAGE
				* (UniqueId.LENGTH + MAX_CONTENT_TYPE_BYTES) + 4);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}
	@Test
	public void testAttachmentFitsIntoRecord() throws Exception {
		String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
		byte[] data = getRandomBytes(MAX_IMAGE_SIZE);
		ByteArrayInputStream dataIn = new ByteArrayInputStream(data);
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		byte[] descriptor =
				clientHelper.toByteArray(BdfList.of(ATTACHMENT, contentType));
		bodyOut.write(descriptor);
		copyAndClose(dataIn, bodyOut);
		byte[] body = bodyOut.toByteArray();
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		Message message =
				messageFactory.createMessage(groupId, timestamp, body);
		int length = message.getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8
				+ 1 + MAX_CONTENT_TYPE_BYTES + MAX_IMAGE_SIZE);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}
	@Test
	public void testForumPostFitsIntoRecord() throws Exception {
		String authorName = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		LocalAuthor author = authorFactory.createLocalAuthor(authorName);
		GroupId groupId = new GroupId(getRandomId());
		long timestamp = Long.MAX_VALUE;
		MessageId parent = new MessageId(getRandomId());
		String text = getRandomString(MAX_FORUM_POST_TEXT_LENGTH);
		ForumPost post = forumPostFactory.createPost(groupId,
				timestamp, parent, author, text);
		int length = post.getMessage().getRawLength();
		assertTrue(length > UniqueId.LENGTH + 8 + UniqueId.LENGTH + 4
				+ MAX_AUTHOR_NAME_LENGTH + MAX_PUBLIC_KEY_LENGTH
				+ MAX_FORUM_POST_TEXT_LENGTH);
		assertTrue(length <= MAX_RECORD_PAYLOAD_BYTES);
	}
}