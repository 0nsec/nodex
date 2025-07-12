package org.nodex.android.forum;
import android.app.Application;
import android.widget.Toast;
import org.nodex.core.api.contact.event.ContactRemovedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.event.GroupAddedEvent;
import org.nodex.core.api.sync.event.GroupRemovedEvent;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.R;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveResult;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.forum.Forum;
import org.nodex.api.forum.ForumManager;
import org.nodex.api.forum.ForumPostHeader;
import org.nodex.api.forum.ForumSharingManager;
import org.nodex.api.forum.event.ForumInvitationRequestReceivedEvent;
import org.nodex.api.forum.event.ForumPostReceivedEvent;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static android.widget.Toast.LENGTH_SHORT;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.now;
import static org.nodex.api.forum.ForumManager.CLIENT_ID;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class ForumListViewModel extends DbViewModel implements EventListener {
	private static final Logger LOG =
			getLogger(ForumListViewModel.class.getName());
	private final ForumManager forumManager;
	private final ForumSharingManager forumSharingManager;
	private final AndroidNotificationManager notificationManager;
	private final EventBus eventBus;
	private final MutableLiveData<LiveResult<List<ForumListItem>>> forumItems =
			new MutableLiveData<>();
	private final MutableLiveData<Integer> numInvitations =
			new MutableLiveData<>();
	@Inject
	ForumListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			ForumManager forumManager,
			ForumSharingManager forumSharingManager,
			AndroidNotificationManager notificationManager, EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.forumManager = forumManager;
		this.forumSharingManager = forumSharingManager;
		this.notificationManager = notificationManager;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}
	void clearAllForumPostNotifications() {
		notificationManager.clearAllForumPostNotifications();
	}
	void blockAllForumPostNotifications() {
		notificationManager.blockAllForumPostNotifications();
	}
	void unblockAllForumPostNotifications() {
		notificationManager.unblockAllForumPostNotifications();
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, reloading available forums");
			loadForumInvitations();
		} else if (e instanceof ForumInvitationRequestReceivedEvent) {
			LOG.info("Forum invitation received, reloading available forums");
			loadForumInvitations();
		} else if (e instanceof GroupAddedEvent) {
			GroupAddedEvent g = (GroupAddedEvent) e;
			if (g.getGroup().getClientId().equals(CLIENT_ID)) {
				LOG.info("Forum added, reloading forums");
				loadForums();
			}
		} else if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getClientId().equals(CLIENT_ID)) {
				LOG.info("Forum removed, removing from list");
				onGroupRemoved(g.getGroup().getId());
			}
		} else if (e instanceof ForumPostReceivedEvent) {
			ForumPostReceivedEvent f = (ForumPostReceivedEvent) e;
			LOG.info("Forum post added, updating item");
			onForumPostReceived(f.getGroupId(), f.getHeader());
		}
	}
	void loadForums() {
		loadFromDb(this::loadForums, forumItems::setValue);
	}
	@DatabaseExecutor
	private List<ForumListItem> loadForums(Transaction txn) throws DbException {
		long start = now();
		List<ForumListItem> forums = new ArrayList<>();
		for (Forum f : forumManager.getForums(txn)) {
			GroupCount count = forumManager.getGroupCount(txn, f.getId());
			forums.add(new ForumListItem(f, count));
		}
		Collections.sort(forums);
		logDuration(LOG, "Loading forums", start);
		return forums;
	}
	@UiThread
	private void onForumPostReceived(GroupId g, ForumPostHeader header) {
		List<ForumListItem> list = updateListItems(getList(forumItems),
				itemToTest -> itemToTest.getForum().getId().equals(g),
				itemToUpdate -> new ForumListItem(itemToUpdate, header));
		if (list == null) return;
		Collections.sort(list);
		forumItems.setValue(new LiveResult<>(list));
	}
	@UiThread
	private void onGroupRemoved(GroupId groupId) {
		removeAndUpdateListItems(forumItems, i ->
				i.getForum().getId().equals(groupId)
		);
	}
	void loadForumInvitations() {
		runOnDbThread(() -> {
			try {
				long start = now();
				int available = forumSharingManager.getInvitations().size();
				logDuration(LOG, "Loading available", start);
				numInvitations.postValue(available);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<LiveResult<List<ForumListItem>>> getForumListItems() {
		return forumItems;
	}
	LiveData<Integer> getNumInvitations() {
		return numInvitations;
	}
	void deleteForum(GroupId groupId) {
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