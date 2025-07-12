package org.nodex.api.identity;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.core.test.BrambleTestCase;
import org.nodex.api.attachment.AttachmentHeader;
import org.junit.Test;
import static org.nodex.core.test.TestUtils.getRandomId;
import static org.nodex.core.util.StringUtils.getRandomString;
import static org.nodex.api.attachment.MediaConstants.MAX_CONTENT_TYPE_BYTES;
import static org.nodex.api.identity.AuthorInfo.Status.NONE;
import static org.nodex.api.identity.AuthorInfo.Status.VERIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
public class AuthorInfoTest extends BrambleTestCase {
	private final String contentType = getRandomString(MAX_CONTENT_TYPE_BYTES);
	private final AttachmentHeader avatarHeader =
			new AttachmentHeader(new GroupId(getRandomId()),
					new MessageId(getRandomId()), contentType);
	@Test
	public void testEquals() {
		assertEquals(
				new AuthorInfo(NONE),
				new AuthorInfo(NONE, null, null)
		);
		assertEquals(
				new AuthorInfo(NONE, "test", null),
				new AuthorInfo(NONE, "test", null)
		);
		assertEquals(
				new AuthorInfo(NONE, "test", avatarHeader),
				new AuthorInfo(NONE, "test", avatarHeader)
		);
		assertNotEquals(
				new AuthorInfo(NONE),
				new AuthorInfo(VERIFIED)
		);
		assertNotEquals(
				new AuthorInfo(NONE, "test", null),
				new AuthorInfo(NONE)
		);
		assertNotEquals(
				new AuthorInfo(NONE),
				new AuthorInfo(NONE, "test", null)
		);
		assertNotEquals(
				new AuthorInfo(NONE, "a", null),
				new AuthorInfo(NONE, "b", null)
		);
		assertNotEquals(
				new AuthorInfo(NONE, "a", null),
				new AuthorInfo(NONE, "a", avatarHeader)
		);
	}
}