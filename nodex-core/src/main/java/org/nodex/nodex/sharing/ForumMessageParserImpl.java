package org.nodex.sharing;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.client.ClientHelper;
import org.nodex.core.api.data.BdfList;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumFactory;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
@Immutable
@NotNullByDefault
class ForumMessageParserImpl extends MessageParserImpl<Forum> {
	private final ForumFactory forumFactory;
	@Inject
	ForumMessageParserImpl(ClientHelper clientHelper,
			ForumFactory forumFactory) {
		super(clientHelper);
		this.forumFactory = forumFactory;
	}
	@Override
	public Forum createShareable(BdfList descriptor) throws FormatException {
		String name = descriptor.getString(0);
		byte[] salt = descriptor.getRaw(1);
		return forumFactory.createForum(name, salt);
	}
}