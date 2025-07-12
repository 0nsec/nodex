package org.nodex.android.contact;
import android.app.Application;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.contact.event.ContactAddedEvent;
import org.nodex.core.api.contact.event.ContactAliasChangedEvent;
import org.nodex.core.api.contact.event.ContactRemovedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.plugin.event.ContactConnectedEvent;
import org.nodex.core.api.plugin.event.ContactDisconnectedEvent;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveResult;
import org.nodex.api.avatar.event.AvatarUpdatedEvent;
import org.nodex.api.client.MessageTracker;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.conversation.event.ConversationMessageTrackedEvent;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.identity.AuthorManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.util.LogUtils.logDuration;
import static org.nodex.core.util.LogUtils.now;
@NotNullByDefault
public class ContactsViewModel extends DbViewModel implements EventListener {
	private static final Logger LOG =
			getLogger(ContactsViewModel.class.getName());
	protected final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final ConversationManager conversationManager;
	private final ConnectionRegistry connectionRegistry;
	private final EventBus eventBus;
	private final MutableLiveData<LiveResult<List<ContactListItem>>>
			contactListItems = new MutableLiveData<>();
	@Inject
	public ContactsViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.conversationManager = conversationManager;
		this.connectionRegistry = connectionRegistry;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}
	protected void loadContacts() {
		loadFromDb(this::loadContacts, contactListItems::setValue);
	}
	private List<ContactListItem> loadContacts(Transaction txn)
			throws DbException {
		long start = now();
		List<ContactListItem> contacts = new ArrayList<>();
		for (Contact c : contactManager.getContacts(txn)) {
			ContactId id = c.getId();
			if (!displayContact(id)) {
				continue;
			}
			AuthorInfo authorInfo = authorManager.getAuthorInfo(txn, c);
			MessageTracker.GroupCount count =
					conversationManager.getGroupCount(txn, id);
			boolean connected = connectionRegistry.isConnected(c.getId());
			contacts.add(new ContactListItem(c, authorInfo, connected, count));
		}
		Collections.sort(contacts);
		logDuration(LOG, "Full load", start);
		return contacts;
	}
	protected boolean displayContact(ContactId contactId) {
		return true;
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactAddedEvent) {
			LOG.info("Contact added, reloading");
			loadContacts();
		} else if (e instanceof ContactConnectedEvent) {
			updateItem(((ContactConnectedEvent) e).getContactId(),
					item -> new ContactListItem(item, true), false);
		} else if (e instanceof ContactDisconnectedEvent) {
			updateItem(((ContactDisconnectedEvent) e).getContactId(),
					item -> new ContactListItem(item, false), false);
		} else if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, removing item");
			removeItem(((ContactRemovedEvent) e).getContactId());
		} else if (e instanceof ConversationMessageTrackedEvent) {
			LOG.info("Conversation message tracked, updating item");
			ConversationMessageTrackedEvent p =
					(ConversationMessageTrackedEvent) e;
			long timestamp = p.getTimestamp();
			boolean read = p.getRead();
			updateItem(p.getContactId(),
					item -> new ContactListItem(item, timestamp, read), true);
		} else if (e instanceof AvatarUpdatedEvent) {
			AvatarUpdatedEvent a = (AvatarUpdatedEvent) e;
			updateItem(a.getContactId(), item -> new ContactListItem(item,
					a.getAttachmentHeader()), false);
		} else if (e instanceof ContactAliasChangedEvent) {
			ContactAliasChangedEvent c = (ContactAliasChangedEvent) e;
			updateItem(c.getContactId(),
					item -> new ContactListItem(item, c.getAlias()), false);
		}
	}
	public LiveData<LiveResult<List<ContactListItem>>> getContactListItems() {
		return contactListItems;
	}
	@UiThread
	private void updateItem(ContactId c,
			Function<ContactListItem, ContactListItem> replacer, boolean sort) {
		List<ContactListItem> list = updateListItems(getList(contactListItems),
				itemToTest -> itemToTest.getContact().getId().equals(c),
				replacer);
		if (list == null) return;
		if (sort) Collections.sort(list);
		contactListItems.setValue(new LiveResult<>(list));
	}
	@UiThread
	private void removeItem(ContactId c) {
		removeAndUpdateListItems(contactListItems,
				itemToTest -> itemToTest.getContact().getId().equals(c));
	}
}