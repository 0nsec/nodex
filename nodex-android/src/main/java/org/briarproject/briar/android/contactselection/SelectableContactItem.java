package org.nodex.android.contactselection;
import org.nodex.core.api.contact.Contact;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.sharing.SharingManager.SharingStatus;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
import static org.nodex.api.sharing.SharingManager.SharingStatus.SHAREABLE;
@NotThreadSafe
@NotNullByDefault
public class SelectableContactItem extends BaseSelectableContactItem {
	private final SharingStatus sharingStatus;
	public SelectableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected, SharingStatus sharingStatus) {
		super(contact, authorInfo, selected);
		this.sharingStatus = sharingStatus;
	}
	public SharingStatus getSharingStatus() {
		return sharingStatus;
	}
	@Override
	public boolean isDisabled() {
		return sharingStatus != SHAREABLE;
	}
}