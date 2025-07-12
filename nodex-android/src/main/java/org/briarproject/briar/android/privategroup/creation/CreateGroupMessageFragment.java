package org.nodex.android.privategroup.creation;
import org.nodex.R;
import org.nodex.android.sharing.BaseMessageFragment;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import androidx.annotation.StringRes;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateGroupMessageFragment extends BaseMessageFragment {
	private final static String TAG =
			CreateGroupMessageFragment.class.getName();
	@Override
	@StringRes
	protected int getButtonText() {
		return R.string.groups_create_group_invitation_button;
	}
	@Override
	@StringRes
	protected int getHintText() {
		return R.string.forum_share_message;
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
}