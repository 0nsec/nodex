package org.nodex.android.sharing;
import android.view.View;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.util.StringUtils;
import org.nodex.R;
import org.nodex.android.sharing.InvitationAdapter.InvitationClickListener;
import org.nodex.api.sharing.SharingInvitationItem;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;
import static org.nodex.android.util.UiUtils.getContactDisplayName;
class SharingInvitationViewHolder
		extends InvitationViewHolder<SharingInvitationItem> {
	SharingInvitationViewHolder(View v) {
		super(v);
	}
	@Override
	public void onBind(@Nullable SharingInvitationItem item,
			InvitationClickListener<SharingInvitationItem> listener) {
		super.onBind(item, listener);
		if (item == null) return;
		Collection<String> names = new ArrayList<>();
		for (Contact c : item.getNewSharers())
			names.add(getContactDisplayName(c));
		sharedBy.setText(
				sharedBy.getContext().getString(R.string.shared_by_format,
						StringUtils.join(names, ", ")));
	}
}