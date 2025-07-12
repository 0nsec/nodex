package org.nodex.android.privategroup.reveal;
import org.nodex.core.api.contact.Contact;
import org.nodex.android.contactselection.BaseSelectableContactItem;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.api.privategroup.Visibility;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
import static org.nodex.api.privategroup.Visibility.INVISIBLE;
@NotThreadSafe
@NotNullByDefault
class RevealableContactItem extends BaseSelectableContactItem {
	private final Visibility visibility;
	RevealableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected, Visibility visibility) {
		super(contact, authorInfo, selected);
		this.visibility = visibility;
	}
	Visibility getVisibility() {
		return visibility;
	}
	@Override
	public boolean isDisabled() {
		return visibility != INVISIBLE;
	}
}