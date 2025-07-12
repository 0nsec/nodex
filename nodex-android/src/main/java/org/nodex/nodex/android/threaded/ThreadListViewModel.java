package org.nodex.android.threaded;
import android.app.Application;
import org.nodex.core.api.crypto.CryptoExecutor;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchGroupException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.sync.event.GroupRemovedEvent;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.api.system.Clock;
import org.nodex.android.sharing.SharingController;
import org.nodex.android.sharing.SharingController.SharingInfo;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveResult;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTree;
import org.nodex.client.MessageTreeImpl;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ThreadListViewModel<I extends ThreadItem>
		extends DbViewModel implements EventListener {
	private static final Logger LOG =
			getLogger(ThreadListViewModel.class.getName());
	protected final IdentityManager identityManager;
	protected final AndroidNotificationManager notificationManager;
	protected final SharingController sharingController;
	protected final Executor cryptoExecutor;
	protected final Clock clock;
	private final MessageTracker messageTracker;
	private final EventBus eventBus;
	private final MessageTree<I> messageTree = new MessageTreeImpl<>();
	private final MutableLiveData<LiveResult<List<I>>> items =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> groupRemoved =
			new MutableLiveData<>();
	private final AtomicReference<MessageId> scrollToItem =
			new AtomicReference<>();
	protected volatile GroupId groupId;
	@Nullable
	private MessageId replyId;
	private final AtomicReference<MessageId> storedMessageId =
			new AtomicReference<>();
	public ThreadListViewModel(Application application,
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
			EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.identityManager = identityManager;
		this.notificationManager = notificationManager;
		this.cryptoExecutor = cryptoExecutor;
		this.clock = clock;
		this.sharingController = sharingController;
		this.messageTracker = messageTracker;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
		sharingController.onCleared();
	}
	public final void setGroupId(GroupId groupId) {
		boolean needsInitialLoad = this.groupId == null;
		this.groupId = groupId;
		if (needsInitialLoad) performInitialLoad();
	}
	@CallSuper
	protected void performInitialLoad() {
		loadStoredMessageId();
		loadItems();
		loadSharingContacts();
	}
	protected abstract void clearNotifications();
	void blockAndClearNotifications() {
		notificationManager.blockNotification(groupId);
		clearNotifications();
	}
	void unblockNotifications() {
		notificationManager.unblockNotification(groupId);
	}
	@Override
	@CallSuper
	public void eventOccurred(Event e) {
		if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent s = (GroupRemovedEvent) e;
			if (s.getGroup().getId().equals(groupId)) {
				LOG.info("Group removed");
				groupRemoved.setValue(true);
			}
		}
	}
	private void loadStoredMessageId() {
		runOnDbThread(() -> {
			try {
				storedMessageId
						.set(messageTracker.loadStoredMessageId(groupId));
				if (LOG.isLoggable(INFO)) {
					LOG.info("Loaded last top visible message id " +
							storedMessageId);
				}
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	public abstract void loadItems();
	public abstract void createAndStoreMessage(String text,
			@Nullable MessageId parentMessageId);
	protected abstract void loadSharingContacts();
	@UiThread
	protected void setItems(LiveResult<List<I>> items) {
		if (items.hasError()) {
			this.items.setValue(items);
		} else {
			messageTree.clear();
			messageTree.add(requireNonNull(items.getResultOrNull()));
			LiveResult<List<I>> result =
					new LiveResult<>(messageTree.depthFirstOrder());
			this.items.setValue(result);
		}
	}
	@UiThread
	protected void addItem(I item, boolean scrollToItem) {
		if (items.getValue() == null) return;
		messageTree.add(item);
		if (scrollToItem) this.scrollToItem.set(item.getId());
		items.setValue(new LiveResult<>(messageTree.depthFirstOrder()));
	}
	@UiThread
	void setReplyId(@Nullable MessageId id) {
		replyId = id;
	}
	@UiThread
	@Nullable
	MessageId getReplyId() {
		return replyId;
	}
	@UiThread
	void storeMessageId(@Nullable MessageId messageId) {
		if (messageId != null) {
			runOnDbThread(() -> {
				try {
					messageTracker.storeMessageId(groupId, messageId);
				} catch (NoSuchGroupException e) {
				} catch (DbException e) {
					handleException(e);
				}
			});
		}
	}
	protected abstract void markItemRead(I item);
	@Nullable
	MessageId getAndResetRestoredMessageId() {
		return storedMessageId.getAndSet(null);
	}
	LiveData<LiveResult<List<I>>> getItems() {
		return items;
	}
	LiveData<SharingInfo> getSharingInfo() {
		return sharingController.getSharingInfo();
	}
	LiveData<Boolean> getGroupRemoved() {
		return groupRemoved;
	}
	@Nullable
	MessageId getAndResetScrollToItem() {
		return scrollToItem.getAndSet(null);
	}
}