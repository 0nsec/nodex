package org.nodex.core.blog;

import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.data.BdfList;
import org.nodex.api.identity.Author;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogFactory;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.nodex.api.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class BlogFactoryImpl implements BlogFactory {

    private final GroupFactory groupFactory;
    private final ClientHelper clientHelper;

    @Inject
    BlogFactoryImpl(GroupFactory groupFactory, ClientHelper clientHelper) {
        this.groupFactory = groupFactory;
        this.clientHelper = clientHelper;
    }

    @Override
    public Blog createBlog(Author a) {
        return createBlog(a, false);
    }

    @Override
    public Blog createFeedBlog(Author a) {
        return createBlog(a, true);
    }

    private Blog createBlog(Author a, boolean rssFeed) {
        try {
            BdfList blog = BdfList.of(clientHelper.toList(a), rssFeed);
            byte[] descriptor = clientHelper.toByteArray(blog);
            Group g = groupFactory.createGroup(BlogManager.CLIENT_ID, 
                    BlogManager.MAJOR_VERSION, descriptor);
            return new Blog(g, a, rssFeed);
        } catch (FormatException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Blog parseBlog(Group g) throws FormatException {
        // Author, RSS feed
        BdfList descriptor = clientHelper.toList(g.getDescriptor());
        checkSize(descriptor, 2);
        BdfList authorList = descriptor.getList(0);
        boolean rssFeed = descriptor.getBoolean(1);

        Author author = clientHelper.parseAndValidateAuthor(authorList);
        return new Blog(g, author, rssFeed);
    }
}
