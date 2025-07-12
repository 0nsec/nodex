package org.nodex.android.contactselection;
import android.content.Context;
import org.nodex.core.api.contact.ContactId;
import org.nodex.android.contact.BaseContactListAdapter;
import org.nodex.android.contact.ContactItemViewHolder;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.ArrayList;
import java.util.Collection;
@NotNullByDefault
public abstract class BaseContactSelectorAdapter<I extends BaseSelectableContactItem, H extends ContactItemViewHolder<I>>
		extends BaseContactListAdapter<I, H> {
	public BaseContactSelectorAdapter(Context context, Class<I> c,
			OnContactClickListener<I> listener) {
		super(context, c, listener);
	}
	public Collection<ContactId> getSelectedContactIds() {
		Collection<ContactId> selected = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			BaseSelectableContactItem item = items.get(i);
			if (item.isSelected()) selected.add(item.getContact().getId());
		}
		return selected;
	}
}