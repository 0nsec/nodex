package org.nodex.android.sharing;
import org.nodex.core.api.connection.ConnectionRegistry;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.plugin.event.ContactConnectedEvent;
import org.nodex.core.api.plugin.event.ContactDisconnectedEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
@NotNullByDefault
public class SharingControllerImpl implements SharingController, EventListener {
	private final EventBus eventBus;
	private final ConnectionRegistry connectionRegistry;
	private final Set<ContactId> contacts = new HashSet<>();
	private final MutableLiveData<SharingInfo> sharingInfo =
			new MutableLiveData<>();
	@Inject
	SharingControllerImpl(EventBus eventBus,
			ConnectionRegistry connectionRegistry) {
		this.eventBus = eventBus;
		this.connectionRegistry = connectionRegistry;
		eventBus.addListener(this);
	}
	@Override
	public void onCleared() {
		eventBus.removeListener(this);
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactConnectedEvent) {
			setConnected(((ContactConnectedEvent) e).getContactId());
		} else if (e instanceof ContactDisconnectedEvent) {
			setConnected(((ContactDisconnectedEvent) e).getContactId());
		}
	}
	@UiThread
	private void setConnected(ContactId c) {
		if (contacts.contains(c)) {
			updateLiveData();
		}
	}
	@UiThread
	private void updateLiveData() {
		int online = getOnlineCount();
		sharingInfo.setValue(new SharingInfo(contacts.size(), online));
	}
	private int getOnlineCount() {
		int online = 0;
		for (ContactId c : contacts) {
			if (connectionRegistry.isConnected(c)) online++;
		}
		return online;
	}
	@UiThread
	@Override
	public void addAll(Collection<ContactId> c) {
		contacts.addAll(c);
		updateLiveData();
	}
	@UiThread
	@Override
	public void add(ContactId c) {
		contacts.add(c);
		updateLiveData();
	}
	@UiThread
	@Override
	public void remove(ContactId c) {
		contacts.remove(c);
		updateLiveData();
	}
	@Override
	public LiveData<SharingInfo> getSharingInfo() {
		return sharingInfo;
	}
}