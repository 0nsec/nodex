package org.nodex.android.privategroup.conversation;
import android.app.Application;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.crypto.CryptoExecutor;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.identity.IdentityManager;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.sync.GroupId;
import org.nodex.core.api.sync.MessageId;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.api.system.Clock;
import org.nodex.android.sharing.SharingController;
import org.nodex.android.threaded.ThreadListViewModel;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.client.MessageTracker.GroupCount;
import org.nodex.api.privategroup.GroupMember;
import org.nodex.api.privategroup.GroupMessage;
import org.nodex.api.privategroup.GroupMessageFactory;
import org.nodex.api.privategroup.GroupMessageHeader;
import org.nodex.api.privategroup.JoinMessageHeader;
import org.nodex.api.privategroup.PrivateGroup;
import org.nodex.api.privategroup.PrivateGroupManager;
import org.nodex.api.privategroup.event.ContactRelationshipRevealedEvent;
import org.nodex.api.privategroup.event.GroupDissolvedEvent;
import org.nodex.api.privategroup.event.GroupInvitationResponseReceivedEvent;
import org.nodex.api.privategroup.event.GroupMessageAddedEvent;
import org.nodex.api.privategroup.invitation.GroupInvitationResponse;
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
import static java.lang.Math.max;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.now;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class GroupViewModel extends ThreadListViewModel<GroupMessageItem> {
	private static final Logger LOG = getLogger(GroupViewModel.class.getName());
	private final PrivateGroupManager privateGroupManager;
	private final GroupMessageFactory groupMessageFactory;
	private final MutableLiveData<PrivateGroup> privateGroup =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> isCreator = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isDissolved =
			new MutableLiveData<>();
	@Inject
	GroupViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			IdentityManager identityManager,
			AndroidNotificationManager notificationManager,
			SharingController sharingController,
			@CryptoExecutor Executor cryptoExecutor,
			Clock clock,
			MessageTracker messageTracker,
			PrivateGroupManager privateGroupManager,
			GroupMessageFactory groupMessageFactory) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				identityManager, notificationManager, sharingController,
				cryptoExecutor, clock, messageTracker, eventBus);
		this.privateGroupManager = privateGroupManager;
		this.groupMessageFactory = groupMessageFactory;
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof GroupMessageAddedEvent) {
			GroupMessageAddedEvent g = (GroupMessageAddedEvent) e;
			if (!g.isLocal() && g.getGroupId().equals(groupId)) {
				LOG.info("Group message received, adding...");
				GroupMessageItem item = buildItem(g.getHeader(), g.getText());
				addItem(item, false);
				if (item instanceof JoinMessageItem &&
						(((JoinMessageItem) item).isInitial())) {
					loadSharingContacts();
				}
			}
		} else if (e instanceof GroupInvitationResponseReceivedEvent) {
			GroupInvitationResponseReceivedEvent g =
					(GroupInvitationResponseReceivedEvent) e;
			GroupInvitationResponse r = g.getMessageHeader();
			if (r.getShareableId().equals(groupId) && r.wasAccepted()) {
				sharingController.add(g.getContactId());
			}
		} else if (e instanceof ContactRelationshipRevealedEvent) {
			ContactRelationshipRevealedEvent c =
					(ContactRelationshipRevealedEvent) e;
			if (c.getGroupId().equals(groupId)) {
				sharingController.add(c.getContactId());
			}
		} else if (e instanceof GroupDissolvedEvent) {
			GroupDissolvedEvent g = (GroupDissolvedEvent) e;
			if (g.getGroupId().equals(groupId)) {
				isDissolved.setValue(true);
			}
		} else {
			super.eventOccurred(e);
		}
	}
	@Override
	protected void performInitialLoad() {
		super.performInitialLoad();
		loadPrivateGroup(groupId);
	}
	@Override
	protected void clearNotifications() {
		notificationManager.clearGroupMessageNotification(groupId);
	}
	private void loadPrivateGroup(GroupId groupId) {
		runOnDbThread(() -> {
			try {
				PrivateGroup g = privateGroupManager.getPrivateGroup(groupId);
				privateGroup.postValue(g);
				Author author = identityManager.getLocalAuthor();
				isCreator.postValue(g.getCreator().equals(author));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@Override
	public void loadItems() {
		loadFromDb(txn -> {
			isDissolved
					.postValue(privateGroupManager.isDissolved(txn, groupId));
			long start = now();
			List<GroupMessageHeader> headers =
					privateGroupManager.getHeaders(txn, groupId);
			logDuration(LOG, "Loading headers", start);
			start = now();
			List<GroupMessageItem> items = new ArrayList<>();
			for (GroupMessageHeader header : headers) {
				items.add(loadItem(txn, header));
			}
			logDuration(LOG, "Loading bodies and creating items", start);
			return items;
		}, this::setItems);
	}
	private GroupMessageItem loadItem(Transaction txn,
			GroupMessageHeader header) throws DbException {
		String text;
		if (header instanceof JoinMessageHeader) {
			text = "";
		} else {
			text = privateGroupManager.getMessageText(txn, header.getId());
		}
		return buildItem(header, text);
	}
	private GroupMessageItem buildItem(GroupMessageHeader header, String text) {
		if (header instanceof JoinMessageHeader) {
			return new JoinMessageItem((JoinMessageHeader) header, text);
		}
		return new GroupMessageItem(header, text);
	}
	@Override
	public void createAndStoreMessage(String text,
			@Nullable MessageId parentId) {
		runOnDbThread(() -> {
			try {
				LocalAuthor author = identityManager.getLocalAuthor();
				MessageId previousMsgId =
						privateGroupManager.getPreviousMsgId(groupId);
				GroupCount count = privateGroupManager.getGroupCount(groupId);
				long timestamp = count.getLatestMsgTime();
				timestamp = max(clock.currentTimeMillis(), timestamp + 1);
				createMessage(text, timestamp, parentId, author, previousMsgId);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	private void createMessage(String text, long timestamp,
			@Nullable MessageId parentId, LocalAuthor author,
			MessageId previousMsgId) {
		cryptoExecutor.execute(() -> {
			LOG.info("Creating group message...");
			GroupMessage msg = groupMessageFactory.createGroupMessage(groupId,
					timestamp, parentId, author, text, previousMsgId);
			storePost(msg, text);
		});
	}
	private void storePost(GroupMessage msg, String text) {
		runOnDbThread(false, txn -> {
			long start = now();
			GroupMessageHeader header =
					privateGroupManager.addLocalMessage(txn, msg);
			logDuration(LOG, "Storing group message", start);
			txn.attach(() ->
					addItem(buildItem(header, text), true)
			);
		}, this::handleException);
	}
	@Override
	protected void markItemRead(GroupMessageItem item) {
		runOnDbThread(() -> {
			try {
				privateGroupManager.setReadFlag(groupId, item.getId(), true);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@Override
	public void loadSharingContacts() {
		runOnDbThread(true, txn -> {
			Collection<GroupMember> members =
					privateGroupManager.getMembers(txn, groupId);
			Collection<ContactId> contactIds = new ArrayList<>();
			for (GroupMember m : members) {
				if (m.getContactId() != null)
					contactIds.add(m.getContactId());
			}
			txn.attach(() -> sharingController.addAll(contactIds));
		}, this::handleException);
	}
	void deletePrivateGroup() {
		runOnDbThread(() -> {
			try {
				privateGroupManager.removePrivateGroup(groupId);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<PrivateGroup> getPrivateGroup() {
		return privateGroup;
	}
	LiveData<Boolean> isCreator() {
		return isCreator;
	}
	LiveData<Boolean> isDissolved() {
		return isDissolved;
	}
}