package org.nodex.android.hotspot;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class HotspotHelpFragment extends Fragment {
	public final static String TAG = HotspotHelpFragment.class.getName();
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_hotspot_help, container, false);
	}
}