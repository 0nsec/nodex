package org.nodex.android.contactselection;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.contact.OnContactClickListener;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
class ContactSelectorAdapter extends
		BaseContactSelectorAdapter<SelectableContactItem, SelectableContactHolder> {
	ContactSelectorAdapter(Context context,
			OnContactClickListener<SelectableContactItem> listener) {
		super(context, SelectableContactItem.class, listener);
	}
	@Override
	public SelectableContactHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(ctx).inflate(
				R.layout.list_item_selectable_contact, viewGroup, false);
		return new SelectableContactHolder(v);
	}
}