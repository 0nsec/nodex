package org.nodex.android.contactselection;
import org.nodex.core.api.contact.Contact;
import org.nodex.android.contact.ContactItem;
import org.nodex.api.identity.AuthorInfo;
import org.nodex.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
@NotThreadSafe
@NotNullByDefault
public abstract class BaseSelectableContactItem extends ContactItem {
	private boolean selected;
	public BaseSelectableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected) {
		super(contact, authorInfo);
		this.selected = selected;
	}
	boolean isSelected() {
		return selected;
	}
	void toggleSelected() {
		selected = !selected;
	}
	public abstract boolean isDisabled();
}