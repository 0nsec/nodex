package org.nodex.android.forum;
import android.app.Application;
import android.widget.Toast;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.crypto.CryptoExecutor;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.api.system.Clock;
import org.nodex.R;
import org.nodex.android.sharing.SharingController;
import org.nodex.android.threaded.ThreadListViewModel;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumInvitationResponse;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumPost;
import org.nodex.api.forum.ForumPostHeader;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationResponseReceivedEvent;
import org.nodex.api.forum.event.ForumPostReceivedEvent;
import org.nodex.api.sharing.event.ContactLeftShareableEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Math.max;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.now;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ForumViewModel extends ThreadListViewModel<ForumPostItem> {
	private static final Logger LOG = getLogger(ForumViewModel.class.getName());
	private final ForumManager forumManager;
	private final ForumSharingManager forumSharingManager;
	@Inject
	ForumViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			IdentityManager identityManager,
			AndroidNotificationManager notificationManager,
			SharingController sharingController,
			@CryptoExecutor Executor cryptoExecutor,
			Clock clock,
			MessageTracker messageTracker,
			EventBus eventBus,
			ForumManager forumManager,
			ForumSharingManager forumSharingManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				identityManager, notificationManager, sharingController,
				cryptoExecutor, clock, messageTracker, eventBus);
		this.forumManager = forumManager;
		this.forumSharingManager = forumSharingManager;
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ForumPostReceivedEvent) {
			ForumPostReceivedEvent f = (ForumPostReceivedEvent) e;
			if (f.getGroupId().equals(groupId)) {
				LOG.info("Forum post received, adding...");
				ForumPostItem item =
						new ForumPostItem(f.getHeader(), f.getText());
				addItem(item, false);
			}
		} else if (e instanceof ForumInvitationResponseReceivedEvent) {
			ForumInvitationResponseReceivedEvent f =
					(ForumInvitationResponseReceivedEvent) e;
			ForumInvitationResponse r = f.getMessageHeader();
			if (r.getShareableId().equals(groupId) && r.wasAccepted()) {
				LOG.info("Forum invitation was accepted");
				sharingController.add(f.getContactId());
			}
		} else if (e instanceof ContactLeftShareableEvent) {
			ContactLeftShareableEvent c = (ContactLeftShareableEvent) e;
			if (c.getGroupId().equals(groupId)) {
				LOG.info("Forum left by contact");
				sharingController.remove(c.getContactId());
			}
		} else {
			super.eventOccurred(e);
		}
	}
	@Override
	protected void clearNotifications() {
		notificationManager.clearForumPostNotification(groupId);
	}
	LiveData<Forum> loadForum() {
		MutableLiveData<Forum> forum = new MutableLiveData<>();
		runOnDbThread(() -> {
			try {
				Forum f = forumManager.getForum(groupId);
				forum.postValue(f);
			} catch (DbException e) {
				handleException(e);
			}
		});
		return forum;
	}
	@Override
	public void loadItems() {
		loadFromDb(txn -> {
			long start = now();
			List<ForumPostHeader> headers =
					forumManager.getPostHeaders(txn, groupId);
			logDuration(LOG, "Loading headers", start);
			start = now();
			List<ForumPostItem> items = new ArrayList<>();
			for (ForumPostHeader header : headers) {
				items.add(loadItem(txn, header));
			}
			logDuration(LOG, "Loading bodies and creating items", start);
			return items;
		}, this::setItems);
	}
	private ForumPostItem loadItem(Transaction txn, ForumPostHeader header)
			throws DbException {
		String text = forumManager.getPostText(txn, header.getId());
		return new ForumPostItem(header, text);
	}
	@Override
	public void createAndStoreMessage(String text,
			@Nullable MessageId parentId) {
		runOnDbThread(() -> {
			try {
				LocalAuthor author = identityManager.getLocalAuthor();
				GroupCount count = forumManager.getGroupCount(groupId);
				long timestamp = max(count.getLatestMsgTime() + 1,
						clock.currentTimeMillis());
				createMessage(text, timestamp, parentId, author);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	private void createMessage(String text, long timestamp,
			@Nullable MessageId parentId, LocalAuthor author) {
		cryptoExecutor.execute(() -> {
			LOG.info("Creating forum post...");
			ForumPost msg = forumManager.createLocalPost(groupId, text,
					timestamp, parentId, author);
			storePost(msg, text);
		});
	}
	private void storePost(ForumPost msg, String text) {
		runOnDbThread(false, txn -> {
			long start = now();
			ForumPostHeader header = forumManager.addLocalPost(txn, msg);
			logDuration(LOG, "Storing forum post", start);
			txn.attach(() -> {
				ForumPostItem item = new ForumPostItem(header, text);
				addItem(item, true);
			});
		}, this::handleException);
	}
	@Override
	protected void markItemRead(ForumPostItem item) {
		runOnDbThread(() -> {
			try {
				forumManager.setReadFlag(groupId, item.getId(), true);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@Override
	public void loadSharingContacts() {
		runOnDbThread(true, txn -> {
			Collection<Contact> contacts =
					forumSharingManager.getSharedWith(txn, groupId);
			Collection<ContactId> contactIds = new ArrayList<>(contacts.size());
			for (Contact c : contacts) contactIds.add(c.getId());
			txn.attach(() -> sharingController.addAll(contactIds));
		}, this::handleException);
	}
	void deleteForum() {
		runOnDbThread(() -> {
			try {
				Forum f = forumManager.getForum(groupId);
				forumManager.removeForum(f);
				androidExecutor.runOnUiThread(() -> Toast
						.makeText(getApplication(), R.string.forum_left_toast,
								LENGTH_SHORT).show());
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
}