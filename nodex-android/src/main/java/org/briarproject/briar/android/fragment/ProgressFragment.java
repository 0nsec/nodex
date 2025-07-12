package org.nodex.android.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.nodex.R;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import androidx.annotation.Nullable;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ProgressFragment extends BaseFragment {
	public static final String TAG = ProgressFragment.class.getName();
	private static final String PROGRESS_MSG = "progressMessage";
	public static ProgressFragment newInstance(String message) {
		ProgressFragment f = new ProgressFragment();
		Bundle args = new Bundle();
		args.putString(PROGRESS_MSG, message);
		f.setArguments(args);
		return f;
	}
	private String progressMessage;
	@Override
	public String getUniqueTag() {
		return TAG;
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = requireArguments();
		progressMessage = args.getString(PROGRESS_MSG);
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_progress, container, false);
		TextView msg = v.findViewById(R.id.progressMessage);
		msg.setText(progressMessage);
		return v;
	}
}