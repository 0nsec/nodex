package org.nodex.android.contact;
import android.app.Application;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.contact.event.PendingContactAddedEvent;
import org.nodex.core.api.contact.event.PendingContactRemovedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.conversation.ConversationManager;
import org.nodex.api.identity.AuthorManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
@NotNullByDefault
class ContactListViewModel extends ContactsViewModel {
	private final AndroidNotificationManager notificationManager;
	private final MutableLiveData<Boolean> hasPendingContacts =
			new MutableLiveData<>();
	@Inject
	ContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus,
			AndroidNotificationManager notificationManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				contactManager, authorManager, conversationManager,
				connectionRegistry, eventBus);
		this.notificationManager = notificationManager;
	}
	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof PendingContactAddedEvent ||
				e instanceof PendingContactRemovedEvent) {
			checkForPendingContacts();
		}
	}
	LiveData<Boolean> getHasPendingContacts() {
		return hasPendingContacts;
	}
	void checkForPendingContacts() {
		runOnDbThread(() -> {
			try {
				boolean hasPending =
						!contactManager.getPendingContacts().isEmpty();
				hasPendingContacts.postValue(hasPending);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	void clearAllContactNotifications() {
		notificationManager.clearAllContactNotifications();
	}
	void clearAllContactAddedNotifications() {
		notificationManager.clearAllContactAddedNotifications();
	}
}