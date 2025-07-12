package org.nodex.android.conversation;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import org.nodex.core.api.contact.Contact;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BaseActivity;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.ViewModelProvider;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static java.util.Objects.requireNonNull;
import static org.nodex.core.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static org.nodex.core.util.StringUtils.toUtf8;
import static org.nodex.android.util.UiUtils.hideSoftKeyboard;
import static org.nodex.android.util.UiUtils.showSoftKeyboard;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AliasDialogFragment extends AppCompatDialogFragment {
	final static String TAG = AliasDialogFragment.class.getName();
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private ConversationViewModel viewModel;
	private TextInputLayout aliasEditLayout;
	private EditText aliasEditText;
	public static AliasDialogFragment newInstance() {
		return new AliasDialogFragment();
	}
	@Override
	public void onAttach(Context ctx) {
		super.onAttach(ctx);
		injectFragment(
				((BaseActivity) requireActivity()).getActivityComponent());
	}
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory)
				.get(ConversationViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, R.style.NodexDialogTheme);
	}
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alias_dialog, container,
				false);
		aliasEditLayout = v.findViewById(R.id.aliasEditLayout);
		aliasEditText = v.findViewById(R.id.aliasEditText);
		Contact contact = requireNonNull(viewModel.getContactItem().getValue())
				.getContact();
		String alias = contact.getAlias();
		aliasEditText.setText(alias);
		if (alias != null) aliasEditText.setSelection(alias.length());
		Button setButton = v.findViewById(R.id.setButton);
		setButton.setOnClickListener(v1 -> onSetButtonClicked());
		Button cancelButton = v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(v1 -> onCancelButtonClicked());
		return v;
	}
	private void onSetButtonClicked() {
		hideSoftKeyboard(aliasEditText);
		String alias = aliasEditText.getText().toString().trim();
		if (toUtf8(alias).length > MAX_AUTHOR_NAME_LENGTH) {
			aliasEditLayout.setError(getString(R.string.name_too_long));
		} else {
			viewModel.setContactAlias(alias);
			getDialog().dismiss();
		}
	}
	private void onCancelButtonClicked() {
		hideSoftKeyboard(aliasEditText);
		getDialog().cancel();
	}
	@Override
	public void onStart() {
		super.onStart();
		requireNonNull(getDialog().getWindow())
				.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		showSoftKeyboard(aliasEditText);
	}
}