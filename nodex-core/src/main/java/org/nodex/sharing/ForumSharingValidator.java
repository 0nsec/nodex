package org.nodex.sharing;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.data.MetadataEncoder;
import org.nodex.api.sync.GroupId;
import org.nodex.api.system.Clock;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumFactory;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static org.nodex.core.util.ValidationUtils.checkLength;
import static org.nodex.core.util.ValidationUtils.checkSize;
import static org.nodex.api.forum.ForumConstants.FORUM_SALT_LENGTH;
import static org.nodex.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;
@Immutable
@NotNullByDefault
class ForumSharingValidator extends SharingValidator {
	private final ForumFactory forumFactory;
	@Inject
	ForumSharingValidator(MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ForumFactory forumFactory) {
		super(messageEncoder, clientHelper, metadataEncoder, clock);
		this.forumFactory = forumFactory;
	}
	@Override
	protected GroupId validateDescriptor(BdfList descriptor)
			throws FormatException {
		checkSize(descriptor, 2);
		String name = descriptor.getString(0);
		checkLength(name, 1, MAX_FORUM_NAME_LENGTH);
		byte[] salt = descriptor.getRaw(1);
		checkLength(salt, FORUM_SALT_LENGTH);
		Forum forum = forumFactory.createForum(name, salt);
		return forum.getId();
	}
}