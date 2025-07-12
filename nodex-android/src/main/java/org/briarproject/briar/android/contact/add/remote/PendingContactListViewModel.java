package org.nodex.android.contact.add.remote;
import android.app.Application;
import org.nodex.core.api.Pair;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.contact.PendingContact;
import org.nodex.core.api.contact.PendingContactId;
import org.nodex.core.api.contact.PendingContactState;
import org.nodex.core.api.contact.event.PendingContactRemovedEvent;
import org.nodex.core.api.contact.event.PendingContactStateChangedEvent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.rendezvous.RendezvousPoller;
import org.nodex.core.api.rendezvous.event.RendezvousPollEvent;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static org.nodex.core.api.contact.PendingContactState.OFFLINE;
@NotNullByDefault
public class PendingContactListViewModel extends DbViewModel
		implements EventListener {
	private final ContactManager contactManager;
	private final RendezvousPoller rendezvousPoller;
	private final EventBus eventBus;
	private final MutableLiveData<Collection<PendingContactItem>>
			pendingContacts = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasInternetConnection =
			new MutableLiveData<>();
	@Inject
	PendingContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			ContactManager contactManager,
			RendezvousPoller rendezvousPoller,
			EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
		this.rendezvousPoller = rendezvousPoller;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}
	void onCreate() {
		if (pendingContacts.getValue() == null) loadPendingContacts();
	}
	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactStateChangedEvent ||
				e instanceof PendingContactRemovedEvent ||
				e instanceof RendezvousPollEvent) {
			loadPendingContacts();
		}
	}
	private void loadPendingContacts() {
		runOnDbThread(() -> {
			try {
				Collection<Pair<PendingContact, PendingContactState>> pairs =
						contactManager.getPendingContacts();
				List<PendingContactItem> items = new ArrayList<>(pairs.size());
				boolean online = pairs.isEmpty();
				for (Pair<PendingContact, PendingContactState> pair : pairs) {
					PendingContact p = pair.getFirst();
					PendingContactState state = pair.getSecond();
					long lastPoll = rendezvousPoller.getLastPollTime(p.getId());
					items.add(new PendingContactItem(p, state, lastPoll));
					online = online || state != OFFLINE;
				}
				pendingContacts.postValue(items);
				hasInternetConnection.postValue(online);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<Collection<PendingContactItem>> getPendingContacts() {
		return pendingContacts;
	}
	void removePendingContact(PendingContactId id) {
		runOnDbThread(() -> {
			try {
				contactManager.removePendingContact(id);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<Boolean> getHasInternetConnection() {
		return hasInternetConnection;
	}
}