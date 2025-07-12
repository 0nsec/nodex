package org.nodex.android.sharing;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.event.EventBus;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
@NotNullByDefault
public interface SharingController {
	void onCleared();
	@UiThread
	void add(ContactId c);
	@UiThread
	void addAll(Collection<ContactId> contacts);
	@UiThread
	void remove(ContactId c);
	LiveData<SharingInfo> getSharingInfo();
	class SharingInfo {
		public final int total, online;
		SharingInfo(int total, int online) {
			this.total = total;
			this.online = online;
		}
	}
}