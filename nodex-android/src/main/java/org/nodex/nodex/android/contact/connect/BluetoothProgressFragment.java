package org.nodex.android.contact.connect;
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
public class BluetoothProgressFragment extends Fragment {
	final static String TAG = BluetoothProgressFragment.class.getName();
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_bluetooth_progress, container, false);
	}
}