package org.nodex.android.login;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import org.nodex.R;
import org.nodex.android.BriarService;
import org.nodex.android.account.SetupActivity;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BaseActivity;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.login.StartupViewModel.State;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME;
import static org.nodex.android.login.StartupViewModel.State.SIGNED_IN;
import static org.nodex.android.login.StartupViewModel.State.SIGNED_OUT;
import static org.nodex.android.login.StartupViewModel.State.STARTED;
import static org.nodex.android.login.StartupViewModel.State.STARTING;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class StartupActivity extends BaseActivity implements
		BaseFragmentListener {
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private StartupViewModel viewModel;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(StartupViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.activity_fragment_container);
		if (!viewModel.accountExists()) {
			viewModel.deleteAccount();
			onAccountDeleted();
			return;
		}
		viewModel.getAccountDeleted().observeEvent(this, deleted -> {
			if (deleted) onAccountDeleted();
		});
		viewModel.getState().observe(this, this::onStateChanged);
	}
	@Override
	public void onStart() {
		super.onStart();
		viewModel.clearSignInNotification();
	}
	@Override
	@SuppressLint("MissingSuperCall")
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	private void onStateChanged(State state) {
		if (state == SIGNED_OUT) {
			showInitialFragment(new PasswordFragment());
		} else if (state == SIGNED_IN || state == STARTING) {
			startService(new Intent(this, BriarService.class));
			showNextFragment(new OpenDatabaseFragment());
		} else if (state == STARTED) {
			setResult(RESULT_OK);
			supportFinishAfterTransition();
			overridePendingTransition(R.anim.screen_new_in,
					R.anim.screen_old_out);
		}
	}
	private void onAccountDeleted() {
		setResult(RESULT_CANCELED);
		finish();
		Intent i = new Intent(this, SetupActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP |
				FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_TASK_ON_HOME);
		startActivity(i);
	}
	@Override
	public void runOnDbThread(Runnable runnable) {
		throw new UnsupportedOperationException();
	}
}