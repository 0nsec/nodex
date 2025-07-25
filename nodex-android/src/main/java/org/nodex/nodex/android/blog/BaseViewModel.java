package org.nodex.android.blog;
import android.app.Application;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveResult;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.blog.Blog;
import org.nodex.api.blog.BlogCommentHeader;
import org.nodex.api.blog.BlogManager;
import org.nodex.api.blog.BlogPostHeader;
import org.nodex.util.HtmlUtils;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.core.util.LogUtils.now;
@NotNullByDefault
abstract class BaseViewModel extends DbViewModel implements EventListener {
	private static final Logger LOG = getLogger(BaseViewModel.class.getName());
	private final EventBus eventBus;
	protected final IdentityManager identityManager;
	protected final AndroidNotificationManager notificationManager;
	protected final BlogManager blogManager;
	protected final MutableLiveData<LiveResult<ListUpdate>> blogPosts =
			new MutableLiveData<>();
	BaseViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			IdentityManager identityManager,
			AndroidNotificationManager notificationManager,
			BlogManager blogManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.eventBus = eventBus;
		this.identityManager = identityManager;
		this.notificationManager = notificationManager;
		this.blogManager = blogManager;
		eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}
	@DatabaseExecutor
	protected List<BlogPostItem> loadBlogPosts(Transaction txn, GroupId groupId)
			throws DbException {
		long start = now();
		List<BlogPostHeader> headers =
				blogManager.getPostHeaders(txn, groupId);
		logDuration(LOG, "Loading headers", start);
		List<BlogPostItem> items = new ArrayList<>(headers.size());
		start = now();
		for (BlogPostHeader h : headers) {
			BlogPostItem item = getItem(txn, h);
			items.add(item);
		}
		logDuration(LOG, "Loading bodies", start);
		return items;
	}
	@DatabaseExecutor
	protected BlogPostItem getItem(Transaction txn, BlogPostHeader h)
			throws DbException {
		String text;
		if (h instanceof BlogCommentHeader) {
			BlogCommentHeader c = (BlogCommentHeader) h;
			BlogCommentItem item = new BlogCommentItem(c);
			text = getPostText(txn, item.getPostHeader().getId());
			item.setText(text);
			return item;
		} else {
			text = getPostText(txn, h.getId());
			return new BlogPostItem(h, text);
		}
	}
	@DatabaseExecutor
	private String getPostText(Transaction txn, MessageId m)
			throws DbException {
		return HtmlUtils.cleanArticle(blogManager.getPostText(txn, m));
	}
	LiveData<LiveResult<BlogPostItem>> loadBlogPost(GroupId g, MessageId m) {
		MutableLiveData<LiveResult<BlogPostItem>> result =
				new MutableLiveData<>();
		runOnDbThread(true, txn -> {
			long start = now();
			BlogPostHeader header = blogManager.getPostHeader(txn, g, m);
			BlogPostItem item = getItem(txn, header);
			logDuration(LOG, "Loading post", start);
			result.postValue(new LiveResult<>(item));
		}, e -> {
			logException(LOG, WARNING, e);
			result.postValue(new LiveResult<>(e));
		});
		return result;
	}
	protected void onBlogPostAdded(BlogPostHeader header, boolean local) {
		runOnDbThread(true, txn -> {
			BlogPostItem item = getItem(txn, header);
			txn.attach(() -> onBlogPostItemAdded(item, local));
		}, this::handleException);
	}
	@UiThread
	private void onBlogPostItemAdded(BlogPostItem item, boolean local) {
		List<BlogPostItem> items = addListItem(getBlogPostItems(), item);
		if (items != null) {
			Collections.sort(items);
			blogPosts.setValue(new LiveResult<>(new ListUpdate(local, items)));
		}
	}
	void repeatPost(BlogPostItem item, @Nullable String comment) {
		runOnDbThread(() -> {
			try {
				LocalAuthor a = identityManager.getLocalAuthor();
				Blog b = blogManager.getPersonalBlog(a);
				BlogPostHeader h = item.getHeader();
				blogManager.addLocalComment(a, b.getId(), comment, h);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<LiveResult<ListUpdate>> getBlogPosts() {
		return blogPosts;
	}
	@UiThread
	@Nullable
	protected List<BlogPostItem> getBlogPostItems() {
		LiveResult<ListUpdate> value = blogPosts.getValue();
		if (value == null) return null;
		ListUpdate result = value.getResultOrNull();
		return result == null ? null : result.getItems();
	}
	@UiThread
	void resetLocalUpdate() {
		LiveResult<ListUpdate> value = blogPosts.getValue();
		if (value == null) return;
		ListUpdate result = value.getResultOrNull();
		result.postAddedWasLocal = null;
	}
	static class ListUpdate {
		@Nullable
		private Boolean postAddedWasLocal;
		private final List<BlogPostItem> items;
		ListUpdate(@Nullable Boolean postAddedWasLocal,
				List<BlogPostItem> items) {
			this.postAddedWasLocal = postAddedWasLocal;
			this.items = items;
		}
		@Nullable
		public Boolean getPostAddedWasLocal() {
			return postAddedWasLocal;
		}
		public List<BlogPostItem> getItems() {
			return items;
		}
	}
}