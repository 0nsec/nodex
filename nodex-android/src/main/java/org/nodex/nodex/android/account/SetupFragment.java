package org.nodex.android.account;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
import static org.nodex.android.util.UiUtils.enterPressed;
import static org.nodex.android.util.UiUtils.showOnboardingDialog;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class SetupFragment extends BaseFragment implements TextWatcher,
		OnEditorActionListener, OnClickListener {
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	SetupViewModel viewModel;
	@Override
	@CallSuper
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity())
				.get(SetupViewModel.class);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.help_action, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_help) {
			showOnboardingDialog(getContext(), getHelpText());
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	protected abstract String getHelpText();
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
	}
	@Override
	public boolean onEditorAction(TextView textView, int actionId,
			@Nullable KeyEvent keyEvent) {
		if (actionId == IME_ACTION_NEXT || actionId == IME_ACTION_DONE ||
				enterPressed(actionId, keyEvent)) {
			onClick(textView);
			return true;
		}
		return false;
	}
	@Override
	public void afterTextChanged(Editable editable) {
	}
}