package org.nodex.android.contactselection;
import org.nodex.core.api.contact.ContactId;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.Collection;
import androidx.annotation.UiThread;
@NotNullByDefault
public interface ContactSelectorListener {
	@UiThread
	void contactsSelected(Collection<ContactId> contacts);
}