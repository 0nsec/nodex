package org.nodex.android.sharing;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.contact.BaseContactListAdapter;
import org.nodex.android.contact.ContactItem;
import org.nodex.android.contact.ContactItemViewHolder;
class SharingStatusAdapter extends
		BaseContactListAdapter<ContactItem, ContactItemViewHolder<ContactItem>> {
	SharingStatusAdapter(Context context) {
		super(context, ContactItem.class, null);
	}
	@Override
	public ContactItemViewHolder<ContactItem> onCreateViewHolder(
			ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.list_item_contact_small, viewGroup, false);
		return new ContactItemViewHolder<>(v);
	}
}