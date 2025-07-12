package org.nodex.core.forum;

import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.util.StringUtils;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumFactory;
import org.nodex.api.forum.ForumConstants;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.nullsafety.NotNullByDefault;

import java.security.SecureRandom;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

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
        if (length > ForumConstants.MAX_FORUM_NAME_LENGTH)
            throw new IllegalArgumentException();
        byte[] salt = new byte[ForumConstants.FORUM_SALT_LENGTH];
        random.nextBytes(salt);
        return createForum(name, salt);
    }

    @Override
    public Forum createForum(String name, byte[] salt) {
        try {
            BdfList forum = BdfList.of(name, salt);
            byte[] descriptor = clientHelper.toByteArray(forum);
            Group g = groupFactory.createGroup(ForumManager.CLIENT_ID, 
                    ForumManager.MAJOR_VERSION, descriptor);
            return new Forum(g, name, salt);
        } catch (FormatException e) {
            throw new AssertionError(e);
        }
    }
}
