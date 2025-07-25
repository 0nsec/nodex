package org.nodex.android.hotspot;
import android.os.Bundle;
import android.view.View;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import static org.nodex.android.util.UiUtils.showFragment;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class HotspotFragment extends AbstractTabsFragment {
	public final static String TAG = HotspotFragment.class.getName();
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		connectedButton.setOnClickListener(v -> showNextFragment());
	}
	@Override
	protected Fragment getFirstFragment() {
		return ManualHotspotFragment.newInstance(true);
	}
	@Override
	protected Fragment getSecondFragment() {
		return QrHotspotFragment.newInstance(true);
	}
	private void showNextFragment() {
		Fragment f = new WebsiteFragment();
		String tag = WebsiteFragment.TAG;
		showFragment(getParentFragmentManager(), f, tag);
	}
}