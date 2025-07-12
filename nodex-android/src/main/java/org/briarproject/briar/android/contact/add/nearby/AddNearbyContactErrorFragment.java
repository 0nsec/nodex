package org.nodex.android.contact.add.nearby;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.util.UiUtils;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.view.View.GONE;
import static org.nodex.android.util.UiUtils.hideViewOnSmallScreen;
import static org.nodex.android.util.UiUtils.onSingleLinkClick;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AddNearbyContactErrorFragment extends BaseFragment {
	public static final String TAG =
			AddNearbyContactErrorFragment.class.getName();
	private static final String ARG_TITLE = "title";
	private static final String ARG_ERROR_MSG = "message";
	private static final String ARG_FEEDBACK = "feedback";
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private AddNearbyContactViewModel viewModel;
	public static AddNearbyContactErrorFragment newInstance(String title,
			String errorMessage, boolean feedback) {
		AddNearbyContactErrorFragment f = new AddNearbyContactErrorFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_ERROR_MSG, errorMessage);
		args.putBoolean(ARG_FEEDBACK, feedback);
		f.setArguments(args);
		return f;
	}
	@Override
	public String getUniqueTag() {
		return TAG;
	}
	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(AddNearbyContactViewModel.class);
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_error_contact_exchange,
				container, false);
		String title = null, errorMessage = null;
		boolean feedback = true;
		Bundle args = getArguments();
		if (args != null) {
			title = args.getString(ARG_TITLE);
			errorMessage = args.getString(ARG_ERROR_MSG);
			feedback = args.getBoolean(ARG_FEEDBACK, true);
		}
		if (title != null) {
			TextView titleView = v.findViewById(R.id.errorTitle);
			titleView.setText(title);
		}
		if (errorMessage != null) {
			TextView messageView = v.findViewById(R.id.errorMessage);
			messageView.setText(errorMessage);
		}
		TextView sendFeedback = v.findViewById(R.id.sendFeedback);
		if (feedback) {
			onSingleLinkClick(sendFeedback, this::triggerFeedback);
		} else {
			sendFeedback.setVisibility(GONE);
		}
		Button tryAgain = v.findViewById(R.id.tryAgainButton);
		tryAgain.setOnClickListener(view -> {
			FragmentActivity activity = requireActivity();
			Intent i = new Intent(activity, AddNearbyContactActivity.class);
			i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(i);
		});
		Button cancel = v.findViewById(R.id.cancelButton);
		cancel.setOnClickListener(view -> finish());
		return v;
	}
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel.stopListening();
	}
	@Override
	public void onStart() {
		super.onStart();
		hideViewOnSmallScreen(requireView().findViewById(R.id.errorIcon));
	}
	private void triggerFeedback() {
		UiUtils.triggerFeedback(requireContext());
		finish();
	}
}