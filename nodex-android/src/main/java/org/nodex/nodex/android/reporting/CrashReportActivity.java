package org.nodex.android.reporting;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BaseActivity;
import org.nodex.android.fragment.BaseFragment;
import org.nodex.android.fragment.BaseFragment.BaseFragmentListener;
import org.nodex.android.logout.HideUiActivity;
import org.nodex.api.android.MemoryStats;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.Objects.requireNonNull;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CrashReportActivity extends BaseActivity
		implements BaseFragmentListener {
	public static final String EXTRA_INITIAL_COMMENT = "initialComment";
	public static final String EXTRA_THROWABLE = "throwable";
	public static final String EXTRA_APP_START_TIME = "appStartTime";
	public static final String EXTRA_APP_LOGCAT = "logcat";
	public static final String EXTRA_MEMORY_STATS = "memoryStats";
	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private ReportViewModel viewModel;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(ReportViewModel.class);
	}
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_report);
		Intent intent = getIntent();
		String initialComment = intent.getStringExtra(EXTRA_INITIAL_COMMENT);
		Throwable t = (Throwable) intent.getSerializableExtra(EXTRA_THROWABLE);
		long appStartTime = intent.getLongExtra(EXTRA_APP_START_TIME, -1);
		byte[] logKey = intent.getByteArrayExtra(EXTRA_APP_LOGCAT);
		MemoryStats memoryStats =
				(MemoryStats) intent.getSerializableExtra(EXTRA_MEMORY_STATS);
		viewModel.init(t, appStartTime, logKey, initialComment, memoryStats);
		viewModel.getShowReport().observeEvent(this, show -> {
			if (show) displayFragment(true);
		});
		viewModel.getCloseReport().observeEvent(this, res -> {
			if (res != 0) {
				Toast.makeText(this, res, LENGTH_LONG).show();
			}
			exit();
		});
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (savedInstanceState == null) displayFragment(viewModel.isFeedback());
	}
	@Override
	public void runOnDbThread(Runnable runnable) {
		throw new AssertionError("deprecated!!!");
	}
	@Override
	@SuppressLint("MissingSuperCall")
	public void onBackPressed() {
		exit();
	}
	private void displayFragment(boolean showReportForm) {
		BaseFragment f;
		if (showReportForm) {
			f = new ReportFormFragment();
			requireNonNull(getSupportActionBar()).show();
		} else {
			f = new CrashFragment();
			requireNonNull(getSupportActionBar()).hide();
		}
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, f, f.getUniqueTag())
				.commit();
	}
	private void exit() {
		if (!viewModel.isFeedback()) {
			Intent i = new Intent(this, HideUiActivity.class);
			i.addFlags(FLAG_ACTIVITY_NEW_TASK
					| FLAG_ACTIVITY_NO_ANIMATION
					| FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				Process.killProcess(Process.myPid());
			}, 5000);
		}
		finish();
	}
}