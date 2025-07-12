package org.nodex.test;
import org.nodex.core.api.crypto.CryptoComponent;
import org.nodex.core.api.crypto.KeyPair;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.identity.AuthorFactory;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.util.Base32;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import java.util.Locale;
import static java.lang.System.arraycopy;
import static org.nodex.core.api.contact.HandshakeLinkConstants.FORMAT_VERSION;
import static org.nodex.core.api.contact.HandshakeLinkConstants.RAW_LINK_BYTES;
import static org.nodex.core.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.junit.Assert.assertEquals;
public class BriarTestUtils {
	public static void assertGroupCount(MessageTracker tracker, GroupId g,
			long msgCount, long unreadCount, long latestMsgTime)
			throws DbException {
		GroupCount groupCount = tracker.getGroupCount(g);
		assertEquals(msgCount, groupCount.getMsgCount());
		assertEquals(unreadCount, groupCount.getUnreadCount());
		assertEquals(latestMsgTime, groupCount.getLatestMsgTime());
	}
	public static void assertGroupCount(MessageTracker tracker, GroupId g,
			long msgCount, long unreadCount) throws DbException {
		GroupCount c1 = tracker.getGroupCount(g);
		assertEquals(msgCount, c1.getMsgCount());
		assertEquals(unreadCount, c1.getUnreadCount());
	}
	public static Author getRealAuthor(AuthorFactory authorFactory) {
		String name = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		return authorFactory.createLocalAuthor(name);
	}
	public static LocalAuthor getRealLocalAuthor(AuthorFactory authorFactory) {
		String name = getRandomString(MAX_AUTHOR_NAME_LENGTH);
		return authorFactory.createLocalAuthor(name);
	}
	public static String getRealHandshakeLink(CryptoComponent cryptoComponent) {
		KeyPair keyPair = cryptoComponent.generateAgreementKeyPair();
		byte[] linkBytes = new byte[RAW_LINK_BYTES];
		byte[] publicKey = keyPair.getPublic().getEncoded();
		linkBytes[0] = FORMAT_VERSION;
		arraycopy(publicKey, 0, linkBytes, 1, RAW_LINK_BYTES - 1);
		return ("nodex:
	}
}