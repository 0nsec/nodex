package org.nodex.android.mailbox;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class OfflineStatusFragment extends OfflineFragment {
	public static final String TAG = OfflineStatusFragment.class.getName();
	@Override
	protected void onTryAgainClicked() {
		viewModel.checkIfOnlineWhenPaired();
	}
}