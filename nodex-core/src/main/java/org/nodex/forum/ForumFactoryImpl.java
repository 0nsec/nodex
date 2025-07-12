package org.nodex.forum;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.core.util.StringUtils;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumFactory;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.SecureRandom;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.api.forum.ForumConstants.FORUM_SALT_LENGTH;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;
import static org.nodex.api.forum.ForumManager.CLIENT_ID;
import static org.nodex.api.forum.ForumManager.MAJOR_VERSION;
@Immutable
@NotNullByDefault
class ForumFactoryImpl implements ForumFactory {
	private final GroupFactory groupFactory;
	private final ClientHelper clientHelper;
	private final SecureRandom random;
	@Inject
	ForumFactoryImpl(GroupFactory groupFactory, ClientHelper clientHelper,
			SecureRandom random) {
		this.groupFactory = groupFactory;
		this.clientHelper = clientHelper;
		this.random = random;
	}
	@Override
	public Forum createForum(String name) {
		int length = StringUtils.toUtf8(name).length;
		if (length == 0) throw new IllegalArgumentException();
		if (length > MAX_FORUM_NAME_LENGTH)
			throw new IllegalArgumentException();
		byte[] salt = new byte[FORUM_SALT_LENGTH];
		random.nextBytes(salt);
		return createForum(name, salt);
	}
	@Override
	public Forum createForum(String name, byte[] salt) {
		try {
			BdfList forum = BdfList.of(name, salt);
			byte[] descriptor = clientHelper.toByteArray(forum);
			Group g = groupFactory.createGroup(CLIENT_ID, MAJOR_VERSION,
					descriptor);
			return new Forum(g, name, salt);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
}