package org.nodex.introduction;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.crypto.PrivateKey;
import org.nodex.api.crypto.PublicKey;
import org.nodex.api.crypto.SecretKey;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.identity.Author;
import org.nodex.api.identity.AuthorFactory;
import org.nodex.api.plugin.TransportId;
import org.nodex.api.properties.TransportProperties;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.transport.KeySetId;
import org.nodex.core.test.BrambleTestCase;
import org.nodex.api.client.SessionId;
import org.nodex.introduction.IntroducerSession.Introducee;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import static org.nodex.core.test.TestUtils.getAgreementPrivateKey;
import static org.nodex.core.test.TestUtils.getAgreementPublicKey;
import static org.nodex.core.test.TestUtils.getRandomBytes;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.test.TestUtils.getTransportId;
import static org.nodex.core.test.TestUtils.getTransportPropertiesMap;
import static org.nodex.api.introduction.Role.INTRODUCEE;
import static org.nodex.api.introduction.Role.INTRODUCER;
import static org.nodex.introduction.IntroduceeSession.Local;
import static org.nodex.introduction.IntroduceeSession.Remote;
import static org.nodex.introduction.IntroduceeState.LOCAL_ACCEPTED;
import static org.nodex.introduction.IntroducerState.AWAIT_AUTHS;
import static org.nodex.introduction.IntroductionConstants.SESSION_KEY_ROLE;
import static org.nodex.test.NodexTestUtils.getRealAuthor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
public class SessionEncoderParserIntegrationTest extends BrambleTestCase {
	@Inject
	ClientHelper clientHelper;
	@Inject
	AuthorFactory authorFactory;
	private final SessionEncoder sessionEncoder;
	private final SessionParser sessionParser;
	private final GroupId groupId1 = new GroupId(getRandomId());
	private final GroupId groupId2 = new GroupId(getRandomId());
	private final SessionId sessionId = new SessionId(getRandomId());
	private final long requestTimestamp = 42;
	private final long localTimestamp = 1337;
	private final long localTimestamp2 = 1338;
	private final long acceptTimestamp = 123456;
	private final long remoteAcceptTimestamp = 1234567;
	private final MessageId lastLocalMessageId = new MessageId(getRandomId());
	private final MessageId lastLocalMessageId2 = new MessageId(getRandomId());
	private final MessageId lastRemoteMessageId = new MessageId(getRandomId());
	private final MessageId lastRemoteMessageId2 = new MessageId(getRandomId());
	private final Author author1;
	private final Author author2;
	private final PublicKey ephemeralPublicKey = getAgreementPublicKey();
	private final PrivateKey ephemeralPrivateKey = getAgreementPrivateKey();
	private final byte[] masterKey = getRandomBytes(SecretKey.LENGTH);
	private final PublicKey remoteEphemeralPublicKey = getAgreementPublicKey();
	private final Map<TransportId, TransportProperties> transportProperties =
			getTransportPropertiesMap(3);
	private final Map<TransportId, TransportProperties>
			remoteTransportProperties = getTransportPropertiesMap(3);
	private final Map<TransportId, KeySetId> transportKeys = new HashMap<>();
	private final byte[] localMacKey = getRandomBytes(SecretKey.LENGTH);
	private final byte[] remoteMacKey = getRandomBytes(SecretKey.LENGTH);
	public SessionEncoderParserIntegrationTest() {
		IntroductionIntegrationTestComponent component =
				DaggerIntroductionIntegrationTestComponent.builder().build();
		IntroductionIntegrationTestComponent.Helper
				.injectEagerSingletons(component);
		component.inject(this);
		sessionEncoder = new SessionEncoderImpl(clientHelper);
		sessionParser = new SessionParserImpl(clientHelper);
		author1 = getRealAuthor(authorFactory);
		author2 = getRealAuthor(authorFactory);
		transportKeys.put(getTransportId(), new KeySetId(1));
		transportKeys.put(getTransportId(), new KeySetId(2));
		transportKeys.put(getTransportId(), new KeySetId(3));
	}
	@Test
	public void testIntroducerSession() throws FormatException {
		IntroducerSession s1 = getIntroducerSession();
		BdfDictionary d = sessionEncoder.encodeIntroducerSession(s1);
		IntroducerSession s2 = sessionParser.parseIntroducerSession(d);
		assertEquals(INTRODUCER, s1.getRole());
		assertEquals(s1.getRole(), s2.getRole());
		assertEquals(sessionId, s1.getSessionId());
		assertEquals(s1.getSessionId(), s2.getSessionId());
		assertEquals(AWAIT_AUTHS, s1.getState());
		assertEquals(s1.getState(), s2.getState());
		assertIntroduceeEquals(s1.getIntroduceeA(), s2.getIntroduceeA());
		assertIntroduceeEquals(s1.getIntroduceeB(), s2.getIntroduceeB());
	}
	@Test
	public void testIntroducerSessionWithNulls() throws FormatException {
		Introducee introducee1 =
				new Introducee(sessionId, groupId1, author1, localTimestamp,
						null, null);
		Introducee introducee2 =
				new Introducee(sessionId, groupId2, author2, localTimestamp2,
						null, null);
		IntroducerSession s1 = new IntroducerSession(sessionId,
				AWAIT_AUTHS, requestTimestamp, introducee1,
				introducee2);
		BdfDictionary d = sessionEncoder.encodeIntroducerSession(s1);
		IntroducerSession s2 = sessionParser.parseIntroducerSession(d);
		assertNull(s1.getIntroduceeA().lastLocalMessageId);
		assertEquals(s1.getIntroduceeA().lastLocalMessageId,
				s2.getIntroduceeA().lastLocalMessageId);
		assertNull(s1.getIntroduceeA().lastRemoteMessageId);
		assertEquals(s1.getIntroduceeA().lastRemoteMessageId,
				s2.getIntroduceeA().lastRemoteMessageId);
		assertNull(s1.getIntroduceeB().lastLocalMessageId);
		assertEquals(s1.getIntroduceeB().lastLocalMessageId,
				s2.getIntroduceeB().lastLocalMessageId);
		assertNull(s1.getIntroduceeB().lastRemoteMessageId);
		assertEquals(s1.getIntroduceeB().lastRemoteMessageId,
				s2.getIntroduceeB().lastRemoteMessageId);
	}
	@Test(expected = FormatException.class)
	public void testIntroducerSessionUnknownRole() throws FormatException {
		IntroducerSession s = getIntroducerSession();
		BdfDictionary d = sessionEncoder.encodeIntroducerSession(s);
		d.put(SESSION_KEY_ROLE, 1337);
		sessionParser.parseIntroducerSession(d);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testIntroducerSessionWrongRole() throws FormatException {
		IntroducerSession s = getIntroducerSession();
		BdfDictionary d = sessionEncoder.encodeIntroducerSession(s);
		d.put(SESSION_KEY_ROLE, INTRODUCEE.getValue());
		sessionParser.parseIntroducerSession(d);
	}
	@Test
	public void testIntroduceeSession() throws FormatException {
		IntroduceeSession s1 = getIntroduceeSession();
		BdfDictionary d = sessionEncoder.encodeIntroduceeSession(s1);
		IntroduceeSession s2 =
				sessionParser.parseIntroduceeSession(groupId1, d);
		assertEquals(LOCAL_ACCEPTED, s1.getState());
		assertEquals(s1.getState(), s2.getState());
		assertEquals(INTRODUCEE, s1.getRole());
		assertEquals(s1.getRole(), s2.getRole());
		assertEquals(sessionId, s1.getSessionId());
		assertEquals(s1.getSessionId(), s2.getSessionId());
		assertEquals(groupId1, s1.getContactGroupId());
		assertEquals(s1.getContactGroupId(), s2.getContactGroupId());
		assertEquals(author1, s1.getIntroducer());
		assertEquals(s1.getIntroducer(), s2.getIntroducer());
		assertArrayEquals(masterKey, s1.getMasterKey());
		assertArrayEquals(s1.getMasterKey(), s2.getMasterKey());
		assertEquals(transportKeys, s1.getTransportKeys());
		assertEquals(s1.getTransportKeys(), s2.getTransportKeys());
		assertEquals(localTimestamp, s1.getLocalTimestamp());
		assertEquals(s1.getLocalTimestamp(), s2.getLocalTimestamp());
		assertEquals(lastLocalMessageId, s1.getLastLocalMessageId());
		assertEquals(s1.getLastLocalMessageId(), s2.getLastLocalMessageId());
		assertEquals(lastRemoteMessageId, s1.getLastRemoteMessageId());
		assertEquals(s1.getLastRemoteMessageId(), s2.getLastRemoteMessageId());
		assertTrue(s1.getLocal().alice);
		assertEquals(s1.getLocal().alice, s2.getLocal().alice);
		assertEquals(lastLocalMessageId, s1.getLocal().lastMessageId);
		assertEquals(s1.getLocal().lastMessageId, s2.getLocal().lastMessageId);
		assertEquals(localTimestamp, s1.getLocal().lastMessageTimestamp);
		assertEquals(s1.getLocal().lastMessageTimestamp,
				s2.getLocal().lastMessageTimestamp);
		PublicKey s1LocalEphemeralPub = s1.getLocal().ephemeralPublicKey;
		PublicKey s2LocalEphemeralPub = s2.getLocal().ephemeralPublicKey;
		assertNotNull(s1LocalEphemeralPub);
		assertNotNull(s2LocalEphemeralPub);
		assertArrayEquals(ephemeralPublicKey.getEncoded(),
				s1LocalEphemeralPub.getEncoded());
		assertArrayEquals(s1LocalEphemeralPub.getEncoded(),
				s2LocalEphemeralPub.getEncoded());
		PrivateKey s1LocalEphemeralPriv = s1.getLocal().ephemeralPrivateKey;
		PrivateKey s2LocalEphemeralPriv = s2.getLocal().ephemeralPrivateKey;
		assertNotNull(s1LocalEphemeralPriv);
		assertNotNull(s2LocalEphemeralPriv);
		assertArrayEquals(ephemeralPrivateKey.getEncoded(),
				s1LocalEphemeralPriv.getEncoded());
		assertArrayEquals(s1LocalEphemeralPriv.getEncoded(),
				s2LocalEphemeralPriv.getEncoded());
		assertEquals(transportProperties, s1.getLocal().transportProperties);
		assertEquals(s1.getLocal().transportProperties,
				s2.getLocal().transportProperties);
		assertEquals(acceptTimestamp, s1.getLocal().acceptTimestamp);
		assertEquals(s1.getLocal().acceptTimestamp,
				s2.getLocal().acceptTimestamp);
		assertArrayEquals(localMacKey, s1.getLocal().macKey);
		assertArrayEquals(s1.getLocal().macKey, s2.getLocal().macKey);
		assertFalse(s1.getRemote().alice);
		assertEquals(s1.getRemote().alice, s2.getRemote().alice);
		assertEquals(author2, s1.getRemote().author);
		assertEquals(s1.getRemote().author, s2.getRemote().author);
		assertEquals(lastRemoteMessageId, s1.getRemote().lastMessageId);
		assertEquals(s1.getRemote().lastMessageId,
				s2.getRemote().lastMessageId);
		PublicKey s1RemoteEphemeralPub = s1.getRemote().ephemeralPublicKey;
		PublicKey s2RemoteEphemeralPub = s2.getRemote().ephemeralPublicKey;
		assertNotNull(s1RemoteEphemeralPub);
		assertNotNull(s2RemoteEphemeralPub);
		assertArrayEquals(remoteEphemeralPublicKey.getEncoded(),
				s1RemoteEphemeralPub.getEncoded());
		assertArrayEquals(s1RemoteEphemeralPub.getEncoded(),
				s2RemoteEphemeralPub.getEncoded());
		assertEquals(remoteTransportProperties,
				s1.getRemote().transportProperties);
		assertEquals(s1.getRemote().transportProperties,
				s2.getRemote().transportProperties);
		assertEquals(remoteAcceptTimestamp, s1.getRemote().acceptTimestamp);
		assertEquals(s1.getRemote().acceptTimestamp,
				s2.getRemote().acceptTimestamp);
		assertArrayEquals(remoteMacKey, s1.getRemote().macKey);
		assertArrayEquals(s1.getRemote().macKey, s2.getRemote().macKey);
	}
	@Test
	public void testIntroduceeSessionWithNulls() throws FormatException {
		IntroduceeSession s1 = IntroduceeSession
				.getInitial(groupId1, sessionId, author1, false, author2);
		BdfDictionary d = sessionEncoder.encodeIntroduceeSession(s1);
		IntroduceeSession s2 =
				sessionParser.parseIntroduceeSession(groupId1, d);
		assertNull(s1.getLastLocalMessageId());
		assertEquals(s1.getLastLocalMessageId(), s2.getLastLocalMessageId());
		assertNull(s1.getLastRemoteMessageId());
		assertEquals(s1.getLastRemoteMessageId(), s2.getLastRemoteMessageId());
		assertNull(s1.getMasterKey());
		assertEquals(s1.getMasterKey(), s2.getMasterKey());
		assertNull(s1.getTransportKeys());
		assertEquals(s1.getTransportKeys(), s2.getTransportKeys());
		assertNull(s1.getLocal().lastMessageId);
		assertEquals(s1.getLocal().lastMessageId, s2.getLocal().lastMessageId);
		assertNull(s1.getLocal().ephemeralPublicKey);
		assertEquals(s1.getLocal().ephemeralPublicKey,
				s2.getLocal().ephemeralPublicKey);
		assertNull(s1.getLocal().ephemeralPrivateKey);
		assertEquals(s1.getLocal().ephemeralPrivateKey,
				s2.getLocal().ephemeralPrivateKey);
		assertNull(s1.getLocal().transportProperties);
		assertEquals(s1.getLocal().transportProperties,
				s2.getLocal().transportProperties);
		assertNull(s1.getLocal().macKey);
		assertEquals(s1.getLocal().macKey, s2.getLocal().macKey);
		assertNull(s1.getRemote().lastMessageId);
		assertEquals(s1.getRemote().lastMessageId,
				s2.getRemote().lastMessageId);
		assertNull(s1.getRemote().ephemeralPublicKey);
		assertEquals(s1.getRemote().ephemeralPublicKey,
				s2.getRemote().ephemeralPublicKey);
		assertNull(s1.getRemote().transportProperties);
		assertEquals(s1.getRemote().transportProperties,
				s2.getRemote().transportProperties);
		assertNull(s1.getRemote().macKey);
		assertEquals(s1.getRemote().macKey, s2.getRemote().macKey);
	}
	@Test(expected = FormatException.class)
	public void testIntroduceeSessionUnknownRole() throws FormatException {
		IntroduceeSession s = getIntroduceeSession();
		BdfDictionary d = sessionEncoder.encodeIntroduceeSession(s);
		d.put(SESSION_KEY_ROLE, 1337);
		sessionParser.parseIntroduceeSession(groupId1, d);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testIntroduceeSessionWrongRole() throws FormatException {
		IntroduceeSession s = getIntroduceeSession();
		BdfDictionary d = sessionEncoder.encodeIntroduceeSession(s);
		d.put(SESSION_KEY_ROLE, INTRODUCER.getValue());
		sessionParser.parseIntroduceeSession(groupId1, d);
	}
	private IntroducerSession getIntroducerSession() {
		Introducee introducee1 =
				new Introducee(sessionId, groupId1, author1, localTimestamp,
						lastLocalMessageId, lastRemoteMessageId);
		Introducee introducee2 =
				new Introducee(sessionId, groupId2, author2, localTimestamp2,
						lastLocalMessageId2, lastRemoteMessageId2);
		return new IntroducerSession(sessionId, AWAIT_AUTHS,
				requestTimestamp, introducee1, introducee2);
	}
	private IntroduceeSession getIntroduceeSession() {
		Local local = new Local(true, lastLocalMessageId, localTimestamp,
				ephemeralPublicKey, ephemeralPrivateKey, transportProperties,
				acceptTimestamp, localMacKey);
		Remote remote = new Remote(false, author2, lastRemoteMessageId,
				remoteEphemeralPublicKey, remoteTransportProperties,
				remoteAcceptTimestamp, remoteMacKey);
		return new IntroduceeSession(sessionId, LOCAL_ACCEPTED,
				requestTimestamp, groupId1, author1, local, remote,
				masterKey, transportKeys);
	}
	private void assertIntroduceeEquals(Introducee i1, Introducee i2) {
		assertEquals(i1.author, i2.author);
		assertEquals(i1.groupId, i2.groupId);
		assertEquals(i1.localTimestamp, i2.localTimestamp);
		assertEquals(i1.lastLocalMessageId, i2.lastLocalMessageId);
		assertEquals(i1.lastRemoteMessageId, i2.lastRemoteMessageId);
	}
}
