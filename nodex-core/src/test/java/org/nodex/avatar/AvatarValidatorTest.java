package org.nodex.avatar;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.BdfReader;
import org.nodex.api.data.BdfReaderFactory;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.db.Metadata;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.InvalidMessageException;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageContext;
import org.nodex.api.system.Clock;
import org.nodex.core.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;
import java.io.InputStream;
import static org.nodex.core.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;
import static org.nodex.core.test.TestUtils.getClientId;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getMessage;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static org.nodex.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static org.nodex.avatar.AvatarConstants.MSG_KEY_VERSION;
import static org.nodex.avatar.AvatarConstants.MSG_TYPE_UPDATE;
import static org.junit.Assert.assertEquals;
public class AvatarValidatorTest extends BrambleMockTestCase {
	private final BdfReaderFactory bdfReaderFactory =
			context.mock(BdfReaderFactory.class);
	private final MetadataEncoder metadataEncoder =
			context.mock(MetadataEncoder.class);
	private final Clock clock = context.mock(Clock.class);
	private final BdfReader reader = context.mock(BdfReader.class);
	private final Group group = getGroup(getClientId(), 123);
	private final Message message = getMessage(group.getId());
	private final long now = message.getTimestamp() + 1000;
	private final String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
	private final long version = System.currentTimeMillis();
	private final BdfDictionary meta = BdfDictionary.of(
			BdfEntry.of(MSG_KEY_VERSION, version),
			BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType),
			BdfEntry.of(MSG_KEY_DESCRIPTOR_LENGTH, 0L)
	);
	private final AvatarValidator validator =
			new AvatarValidator(bdfReaderFactory, metadataEncoder, clock);
	@Test(expected = InvalidMessageException.class)
	public void testRejectsFarFutureTimestamp() throws Exception {
		expectCheckTimestamp(message.getTimestamp() - MAX_CLOCK_DIFFERENCE - 1);
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsEmptyBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(new BdfList());
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooShortBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsUnknownMessageType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE + 1, version, contentType));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsNonLongVersion() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, "foo", contentType));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsNonStringContentType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, 1337));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsEmptyContentType() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, ""));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooLongContentType() throws Exception {
		String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES + 1);
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, contentType));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsTooLongBody() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, version, contentType, 1));
		validator.validateMessage(message, group);
	}
	@Test(expected = InvalidMessageException.class)
	public void testRejectsNegativeVersion() throws Exception {
		expectCheckTimestamp(now);
		expectParseList(BdfList.of(MSG_TYPE_UPDATE, -1, contentType));
		validator.validateMessage(message, group);
	}
	@Test
	public void testAcceptsUpdateMessage() throws Exception {
		testAcceptsUpdateMessage(
				BdfList.of(MSG_TYPE_UPDATE, version, contentType), meta);
	}
	@Test
	public void testAcceptsZeroVersion() throws Exception {
		BdfList body = BdfList.of(MSG_TYPE_UPDATE, 0L, contentType);
		BdfDictionary meta = BdfDictionary.of(
				BdfEntry.of(MSG_KEY_VERSION, 0L),
				BdfEntry.of(MSG_KEY_CONTENT_TYPE, contentType),
				BdfEntry.of(MSG_KEY_DESCRIPTOR_LENGTH, 0L)
		);
		testAcceptsUpdateMessage(body, meta);
	}
	private void testAcceptsUpdateMessage(BdfList body, BdfDictionary meta)
			throws Exception {
		expectCheckTimestamp(now);
		expectParseList(body);
		expectEncodeMetadata(meta);
		MessageContext result = validator.validateMessage(message, group);
		assertEquals(0, result.getDependencies().size());
	}
	private void expectCheckTimestamp(long now) {
		context.checking(new Expectations() {{
			oneOf(clock).currentTimeMillis();
			will(returnValue(now));
		}});
	}
	private void expectParseList(BdfList body) throws Exception {
		context.checking(new Expectations() {{
			oneOf(bdfReaderFactory).createReader(with(any(InputStream.class)));
			will(returnValue(reader));
			oneOf(reader).readList();
			will(returnValue(body));
		}});
	}
	private void expectEncodeMetadata(BdfDictionary meta) throws Exception {
		context.checking(new Expectations() {{
			oneOf(metadataEncoder).encode(meta);
			will(returnValue(new Metadata()));
		}});
	}
}
