package org.nodex.android.privategroup.invitation;
import android.view.View;
import org.nodex.R;
import org.nodex.android.sharing.InvitationAdapter.InvitationClickListener;
import org.nodex.android.sharing.InvitationViewHolder;
import org.nodex.api.privategroup.invitation.GroupInvitationItem;
import javax.annotation.Nullable;
import static org.nodex.android.util.UiUtils.getContactDisplayName;
class GroupInvitationViewHolder
		extends InvitationViewHolder<GroupInvitationItem> {
	GroupInvitationViewHolder(View v) {
		super(v);
	}
	@Override
	public void onBind(@Nullable GroupInvitationItem item,
			InvitationClickListener<GroupInvitationItem> listener) {
		super.onBind(item, listener);
		if (item == null) return;
		sharedBy.setText(
				sharedBy.getContext().getString(R.string.groups_created_by,
						getContactDisplayName(item.getCreator())));
	}
}