package org.nodex.android.removabledrive;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.nodex.R;
import org.nodex.android.fragment.FinalFragment;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ErrorFragment extends FinalFragment {
	public static ErrorFragment newInstance(@StringRes int title,
			@StringRes int text) {
		ErrorFragment f = new ErrorFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, title);
		args.putInt(ARG_ICON, R.drawable.alerts_and_states_error);
		args.putInt(ARG_ICON_TINT, R.color.briar_red_500);
		args.putInt(ARG_TEXT, text);
		f.setArguments(args);
		return f;
	}
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		buttonView.setText(R.string.try_again_button);
		return v;
	}
	@Override
	protected void onBackButtonPressed() {
		Intent i = requireActivity().getIntent();
		i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
}