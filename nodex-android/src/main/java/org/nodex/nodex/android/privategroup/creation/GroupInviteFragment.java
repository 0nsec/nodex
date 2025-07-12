package org.nodex.android.privategroup.creation;
import android.os.Bundle;
import org.nodex.core.api.sync.GroupId;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.android.contactselection.ContactSelectorFragment;
import org.nodex.android.contactselection.SelectableContactItem;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import static org.nodex.android.activity.NodexActivity.GROUP_ID;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupInviteFragment extends ContactSelectorFragment {
	public static final String TAG = GroupInviteFragment.class.getName();
	@Inject
	CreateGroupController controller;
	public static GroupInviteFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		GroupInviteFragment fragment = new GroupInviteFragment();
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().setTitle(R.string.groups_invite_members);
	}
	@Override
	protected ContactSelectorController<SelectableContactItem> getController() {
		return controller;
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
}