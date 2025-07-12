package org.nodex.android.sharing;
import android.os.Bundle;
import org.nodex.core.api.sync.GroupId;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.contactselection.ContactSelectorController;
import org.nodex.android.contactselection.ContactSelectorFragment;
import org.nodex.android.contactselection.SelectableContactItem;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import static org.nodex.android.activity.NodexActivity.GROUP_ID;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ShareForumFragment extends ContactSelectorFragment {
	public static final String TAG = ShareForumFragment.class.getName();
	@Inject
	ShareForumController controller;
	public static ShareForumFragment newInstance(GroupId groupId) {
		Bundle args = new Bundle();
		args.putByteArray(GROUP_ID, groupId.getBytes());
		ShareForumFragment fragment = new ShareForumFragment();
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
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