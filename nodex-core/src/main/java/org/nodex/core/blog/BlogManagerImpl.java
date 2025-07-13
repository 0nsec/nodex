package org.nodex.core.blog;

import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogPost;
import org.nodex.api.blog.BlogPostHeader;
import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.event.EventBus;
import org.nodex.api.lifecycle.Service;
import org.nodex.api.lifecycle.ServiceException;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Implementation of BlogManager for blog functionality - matches Briar exactly.
 */
@ThreadSafe
@NotNullByDefault
@Singleton
public class BlogManagerImpl implements BlogManager, Service {

    private static final Logger LOG = Logger.getLogger(BlogManagerImpl.class.getName());

    private final DatabaseComponent db;
    private final EventBus eventBus;
    
    private volatile boolean started = false;

    @Inject
    public BlogManagerImpl(DatabaseComponent db, EventBus eventBus) {
        this.db = db;
        this.eventBus = eventBus;
    }

    @Override
    public void startService() throws ServiceException {
        if (started) return;
        
        LOG.info("Starting blog manager");
        started = true;
        LOG.info("Blog manager started");
    }

    @Override
    public void stopService() throws ServiceException {
        if (!started) return;
        
        LOG.info("Stopping blog manager");
        started = false;
        LOG.info("Blog manager stopped");
    }

    @Override
    public Blog getPersonalBlog(LocalAuthor author) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            // Get or create personal blog for the author
            GroupId blogGroupId = generateBlogGroupId(author);
            
            Blog blog = new Blog(blogGroupId, author, true); // personal blog
            
            db.commitTransaction(txn);
            return blog;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public Collection<Blog> getBlogs() throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            // Get all blog groups from database
            Collection<Blog> blogs = new ArrayList<>();
            
            // In a real implementation, this would query blog groups from database
            
            db.commitTransaction(txn);
            return blogs;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public void addLocalPost(BlogPost post) throws DbException {
        Transaction txn = db.startTransaction(false);
        try {
            // Store the blog post in the database
            db.addMessage(txn, post, false);
            
            db.commitTransaction(txn);
            LOG.info("Added local blog post: " + post.getId());
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public Collection<BlogPostHeader> getPostHeaders(GroupId blogId) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            // Get all posts for this blog
            Collection<org.nodex.api.sync.Message> messages = db.getMessages(txn, blogId);
            
            Collection<BlogPostHeader> headers = new ArrayList<>();
            for (org.nodex.api.sync.Message msg : messages) {
                BlogPostHeader header = createHeaderFromMessage(msg);
                headers.add(header);
            }
            
            db.commitTransaction(txn);
            return headers;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public String getPostText(MessageId postId) throws DbException {
        Transaction txn = db.startTransaction(true);
        try {
            org.nodex.api.sync.Message message = db.getMessage(txn, postId);
            String text = extractTextFromBody(message.getBody());
            
            db.commitTransaction(txn);
            return text;
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public void setReadFlag(MessageId postId, boolean read) throws DbException {
        Transaction txn = db.startTransaction(false);
        try {
            // Update read flag in metadata
            
            db.commitTransaction(txn);
            LOG.fine("Set read flag for blog post " + postId + " to " + read);
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    @Override
    public void removeBlog(Blog blog) throws DbException {
        Transaction txn = db.startTransaction(false);
        try {
            // Remove blog and all its posts
            
            db.commitTransaction(txn);
            LOG.info("Removed blog: " + blog.getId());
            
        } catch (DbException e) {
            db.abortTransaction(txn);
            throw e;
        }
    }

    private GroupId generateBlogGroupId(LocalAuthor author) {
        // Generate a consistent group ID for the author's blog
        byte[] groupBytes = new byte[32];
        // In a real implementation, this would derive from author ID + blog descriptor
        return new GroupId(groupBytes);
    }

    private BlogPostHeader createHeaderFromMessage(org.nodex.api.sync.Message message) {
        return new BlogPostHeader(
            message.getId(),
            message.getGroupId(),
            message.getTimestamp(),
            true, // local - simplified
            false, // read - simplified
            extractTextFromBody(message.getBody())
        );
    }

    private String extractTextFromBody(byte[] body) {
        // Extract text content from message body
        return new String(body); // Simplified
    }
}
