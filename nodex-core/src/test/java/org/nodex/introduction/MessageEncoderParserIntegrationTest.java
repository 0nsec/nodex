package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.Message;
import org.nodex.api.sync.MessageFactory;
import org.nodex.api.sync.MessageId;
import org.nodex.api.system.Clock;
import org.nodex.core.test.BrambleTestCase;
import org.nodex.api.client.SessionId;
import org.junit.Test;
import java.util.Map;
import javax.inject.Inject;
import static org.nodex.core.api.crypto.CryptoConstants.MAC_BYTES;
import static org.nodex.core.api.crypto.CryptoConstants.MAX_SIGNATURE_BYTES;
import static org.nodex.core.test.TestUtils.getAgreementPublicKey;
import static org.nodex.core.test.TestUtils.getGroup;
import static org.nodex.core.test.TestUtils.getRandomBytes;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.test.TestUtils.getTransportPropertiesMap;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.api.introduction.IntroductionConstants.MAX_INTRODUCTION_TEXT_LENGTH;
import static org.nodex.api.introduction.IntroductionManager.CLIENT_ID;
import static org.nodex.api.introduction.IntroductionManager.MAJOR_VERSION;
import static org.nodex.introduction.MessageType.ABORT;
import static org.nodex.introduction.MessageType.REQUEST;
import static org.nodex.test.NodexTestUtils.getRealAuthor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
public class MessageEncoderParserIntegrationTest extends BrambleTestCase {
	@Inject
	ClientHelper clientHelper;
	@Inject
	MessageFactory messageFactory;
	@Inject
	MetadataEncoder metadataEncoder;
	@Inject
	AuthorFactory authorFactory;
	@Inject
	Clock clock;
	private final MessageEncoder messageEncoder;
	private final MessageParser messageParser;
	private final IntroductionValidator validator;
	private final Group group = getGroup(CLIENT_ID.toString(), MAJOR_VERSION);
	private final GroupId groupId = group.getId();
	private final long timestamp = 42L;
	private final SessionId sessionId = new SessionId(getRandomId());
	private final MessageId previousMsgId = new MessageId(getRandomId());
	private final Author author;
	private final String text = getRandomString(MAX_INTRODUCTION_TEXT_LENGTH);
	private final PublicKey ephemeralPublicKey = getAgreementPublicKey();
	private final byte[] mac = getRandomBytes(MAC_BYTES);
	private final byte[] signature = getRandomBytes(MAX_SIGNATURE_BYTES);
	public MessageEncoderParserIntegrationTest() {
		IntroductionIntegrationTestComponent component =
				DaggerIntroductionIntegrationTestComponent.builder().build();
		IntroductionIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
		messageEncoder = new MessageEncoderImpl(clientHelper, messageFactory);
		messageParser = new MessageParserImpl(clientHelper);
		validator = new IntroductionValidator(messageEncoder, clientHelper,
				metadataEncoder, clock);
		author = getRealAuthor(authorFactory);
	}
	@Test
	public void testRequestMessageMetadata() throws FormatException {
		BdfDictionary d = messageEncoder.encodeRequestMetadata(timestamp,
				MIN_AUTO_DELETE_TIMER_MS);
		MessageMetadata meta = messageParser.parseMetadata(d);
		assertEquals(REQUEST, meta.getMessageType());
		assertNull(meta.getSessionId());
		assertEquals(timestamp, meta.getTimestamp());
		assertFalse(meta.isLocal());
		assertFalse(meta.isRead());
		assertFalse(meta.isVisibleInConversation());
		assertFalse(meta.isAvailableToAnswer());
		assertEquals(MIN_AUTO_DELETE_TIMER_MS, meta.getAutoDeleteTimer());
	}
	@Test
	public void testMessageMetadata() throws FormatException {
		BdfDictionary d = messageEncoder.encodeMetadata(ABORT, sessionId,
				timestamp, false, true, false, MAX_AUTO_DELETE_TIMER_MS, false);
		MessageMetadata meta = messageParser.parseMetadata(d);
		assertEquals(ABORT, meta.getMessageType());
		assertEquals(sessionId, meta.getSessionId());
		assertEquals(timestamp, meta.getTimestamp());
		assertFalse(meta.isLocal());
		assertTrue(meta.isRead());
		assertFalse(meta.isVisibleInConversation());
		assertFalse(meta.isAvailableToAnswer());
		assertEquals(MAX_AUTO_DELETE_TIMER_MS, meta.getAutoDeleteTimer());
	}
	@Test
	public void testRequestMessage() throws FormatException {
		Message m = messageEncoder
				.encodeRequestMessage(groupId, timestamp, previousMsgId, author,
						text);
		validator.validateMessage(m, group, clientHelper.toList(m));
		RequestMessage rm =
				messageParser.parseRequestMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), rm.getMessageId());
		assertEquals(m.getGroupId(), rm.getGroupId());
		assertEquals(m.getTimestamp(), rm.getTimestamp());
		assertEquals(previousMsgId, rm.getPreviousMessageId());
		assertEquals(author, rm.getAuthor());
		assertEquals(text, rm.getText());
		assertEquals(NO_AUTO_DELETE_TIMER, rm.getAutoDeleteTimer());
	}
	@Test
	public void testRequestMessageWithAutoDeleteTimer() throws FormatException {
		Message m = messageEncoder.encodeRequestMessage(groupId, timestamp,
				previousMsgId, author, text, MIN_AUTO_DELETE_TIMER_MS);
		validator.validateMessage(m, group, clientHelper.toList(m));
		RequestMessage rm =
				messageParser.parseRequestMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), rm.getMessageId());
		assertEquals(m.getGroupId(), rm.getGroupId());
		assertEquals(m.getTimestamp(), rm.getTimestamp());
		assertEquals(previousMsgId, rm.getPreviousMessageId());
		assertEquals(author, rm.getAuthor());
		assertEquals(text, rm.getText());
		assertEquals(MIN_AUTO_DELETE_TIMER_MS, rm.getAutoDeleteTimer());
	}
	@Test
	public void testRequestMessageWithoutAutoDeleteTimer()
			throws FormatException {
		Message m = messageEncoder.encodeRequestMessage(groupId, timestamp,
				previousMsgId, author, text, NO_AUTO_DELETE_TIMER);
		validator.validateMessage(m, group, clientHelper.toList(m));
		RequestMessage rm =
				messageParser.parseRequestMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), rm.getMessageId());
		assertEquals(m.getGroupId(), rm.getGroupId());
		assertEquals(m.getTimestamp(), rm.getTimestamp());
		assertEquals(previousMsgId, rm.getPreviousMessageId());
		assertEquals(author, rm.getAuthor());
		assertEquals(text, rm.getText());
		assertEquals(NO_AUTO_DELETE_TIMER, rm.getAutoDeleteTimer());
	}
	@Test
	public void testRequestMessageWithPreviousMsgNull() throws FormatException {
		Message m = messageEncoder
				.encodeRequestMessage(groupId, timestamp, null, author, text);
		validator.validateMessage(m, group, clientHelper.toList(m));
		RequestMessage rm =
				messageParser.parseRequestMessage(m, clientHelper.toList(m));
		assertNull(rm.getPreviousMessageId());
		assertEquals(NO_AUTO_DELETE_TIMER, rm.getAutoDeleteTimer());
	}
	@Test
	public void testRequestMessageWithMsgNull() throws FormatException {
		Message m = messageEncoder
				.encodeRequestMessage(groupId, timestamp, previousMsgId, author,
						null);
		validator.validateMessage(m, group, clientHelper.toList(m));
		RequestMessage rm =
				messageParser.parseRequestMessage(m, clientHelper.toList(m));
		assertNull(rm.getText());
		assertEquals(NO_AUTO_DELETE_TIMER, rm.getAutoDeleteTimer());
	}
	@Test
	public void testAcceptMessage() throws Exception {
		Map<TransportId, TransportProperties> transportProperties =
				getTransportPropertiesMap(2);
		long acceptTimestamp = 1337L;
		Message m = messageEncoder
				.encodeAcceptMessage(groupId, timestamp, previousMsgId,
						sessionId, ephemeralPublicKey, acceptTimestamp,
						transportProperties);
		validator.validateMessage(m, group, clientHelper.toList(m));
		AcceptMessage am =
				messageParser.parseAcceptMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertArrayEquals(ephemeralPublicKey.getEncoded(),
				am.getEphemeralPublicKey().getEncoded());
		assertEquals(acceptTimestamp, am.getAcceptTimestamp());
		assertEquals(transportProperties, am.getTransportProperties());
		assertEquals(NO_AUTO_DELETE_TIMER, am.getAutoDeleteTimer());
	}
	@Test
	public void testAcceptMessageWithAutoDeleteTimer() throws Exception {
		Map<TransportId, TransportProperties> transportProperties =
				getTransportPropertiesMap(2);
		long acceptTimestamp = 1337L;
		Message m = messageEncoder.encodeAcceptMessage(groupId, timestamp,
				previousMsgId, sessionId, ephemeralPublicKey,
				acceptTimestamp, transportProperties, MAX_AUTO_DELETE_TIMER_MS);
		validator.validateMessage(m, group, clientHelper.toList(m));
		AcceptMessage am =
				messageParser.parseAcceptMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertArrayEquals(ephemeralPublicKey.getEncoded(),
				am.getEphemeralPublicKey().getEncoded());
		assertEquals(acceptTimestamp, am.getAcceptTimestamp());
		assertEquals(transportProperties, am.getTransportProperties());
		assertEquals(MAX_AUTO_DELETE_TIMER_MS, am.getAutoDeleteTimer());
	}
	@Test
	public void testAcceptMessageWithoutAutoDeleteTimer() throws Exception {
		Map<TransportId, TransportProperties> transportProperties =
				getTransportPropertiesMap(2);
		long acceptTimestamp = 1337L;
		Message m = messageEncoder.encodeAcceptMessage(groupId, timestamp,
				previousMsgId, sessionId, ephemeralPublicKey,
				acceptTimestamp, transportProperties, NO_AUTO_DELETE_TIMER);
		validator.validateMessage(m, group, clientHelper.toList(m));
		AcceptMessage am =
				messageParser.parseAcceptMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertArrayEquals(ephemeralPublicKey.getEncoded(),
				am.getEphemeralPublicKey().getEncoded());
		assertEquals(acceptTimestamp, am.getAcceptTimestamp());
		assertEquals(transportProperties, am.getTransportProperties());
		assertEquals(NO_AUTO_DELETE_TIMER, am.getAutoDeleteTimer());
	}
	@Test
	public void testDeclineMessage() throws Exception {
		Message m = messageEncoder
				.encodeDeclineMessage(groupId, timestamp, previousMsgId,
						sessionId);
		validator.validateMessage(m, group, clientHelper.toList(m));
		DeclineMessage dm =
				messageParser.parseDeclineMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), dm.getMessageId());
		assertEquals(m.getGroupId(), dm.getGroupId());
		assertEquals(m.getTimestamp(), dm.getTimestamp());
		assertEquals(previousMsgId, dm.getPreviousMessageId());
		assertEquals(sessionId, dm.getSessionId());
		assertEquals(NO_AUTO_DELETE_TIMER, dm.getAutoDeleteTimer());
	}
	@Test
	public void testDeclineMessageWithAutoDeleteTimer() throws Exception {
		Message m = messageEncoder.encodeDeclineMessage(groupId, timestamp,
				previousMsgId, sessionId, MIN_AUTO_DELETE_TIMER_MS);
		validator.validateMessage(m, group, clientHelper.toList(m));
		DeclineMessage dm =
				messageParser.parseDeclineMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), dm.getMessageId());
		assertEquals(m.getGroupId(), dm.getGroupId());
		assertEquals(m.getTimestamp(), dm.getTimestamp());
		assertEquals(previousMsgId, dm.getPreviousMessageId());
		assertEquals(sessionId, dm.getSessionId());
		assertEquals(MIN_AUTO_DELETE_TIMER_MS, dm.getAutoDeleteTimer());
	}
	@Test
	public void testDeclineMessageWithoutAutoDeleteTimer() throws Exception {
		Message m = messageEncoder.encodeDeclineMessage(groupId, timestamp,
				previousMsgId, sessionId, NO_AUTO_DELETE_TIMER);
		validator.validateMessage(m, group, clientHelper.toList(m));
		DeclineMessage dm =
				messageParser.parseDeclineMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), dm.getMessageId());
		assertEquals(m.getGroupId(), dm.getGroupId());
		assertEquals(m.getTimestamp(), dm.getTimestamp());
		assertEquals(previousMsgId, dm.getPreviousMessageId());
		assertEquals(sessionId, dm.getSessionId());
		assertEquals(NO_AUTO_DELETE_TIMER, dm.getAutoDeleteTimer());
	}
	@Test
	public void testAuthMessage() throws Exception {
		Message m = messageEncoder
				.encodeAuthMessage(groupId, timestamp, previousMsgId,
						sessionId, mac, signature);
		validator.validateMessage(m, group, clientHelper.toList(m));
		AuthMessage am =
				messageParser.parseAuthMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertArrayEquals(mac, am.getMac());
		assertArrayEquals(signature, am.getSignature());
		assertEquals(NO_AUTO_DELETE_TIMER, am.getAutoDeleteTimer());
	}
	@Test
	public void testActivateMessage() throws Exception {
		Message m = messageEncoder
				.encodeActivateMessage(groupId, timestamp, previousMsgId,
						sessionId, mac);
		validator.validateMessage(m, group, clientHelper.toList(m));
		ActivateMessage am =
				messageParser.parseActivateMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertArrayEquals(mac, am.getMac());
		assertEquals(NO_AUTO_DELETE_TIMER, am.getAutoDeleteTimer());
	}
	@Test
	public void testAbortMessage() throws Exception {
		Message m = messageEncoder
				.encodeAbortMessage(groupId, timestamp, previousMsgId,
						sessionId);
		validator.validateMessage(m, group, clientHelper.toList(m));
		AbortMessage am =
				messageParser.parseAbortMessage(m, clientHelper.toList(m));
		assertEquals(m.getId(), am.getMessageId());
		assertEquals(m.getGroupId(), am.getGroupId());
		assertEquals(m.getTimestamp(), am.getTimestamp());
		assertEquals(previousMsgId, am.getPreviousMessageId());
		assertEquals(sessionId, am.getSessionId());
		assertEquals(NO_AUTO_DELETE_TIMER, am.getAutoDeleteTimer());
	}
}
