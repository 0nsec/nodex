package org.nodex.android.activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.transition.Transition;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.nodex.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import org.nodex.core.api.system.Wakeful;
import org.nodex.R;
import org.nodex.android.BriarApplication;
import org.nodex.android.account.UnlockActivity;
import org.nodex.android.controller.BriarController;
import org.nodex.android.controller.DbController;
import org.nodex.android.controller.handler.UiResultHandler;
import org.nodex.android.login.StartupActivity;
import org.nodex.android.logout.ExitActivity;
import org.nodex.api.android.LockManager;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.dontkillmelib.DozeUtils.getDozeWhitelistingIntent;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.activity.RequestCodes.REQUEST_DOZE_WHITELISTING;
import static org.nodex.android.activity.RequestCodes.REQUEST_PASSWORD;
import static org.nodex.android.activity.RequestCodes.REQUEST_UNLOCK;
import static org.nodex.android.util.UiUtils.excludeSystemUi;
import static org.nodex.android.util.UiUtils.isSamsung7;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class BriarActivity extends BaseActivity {
	public static final String GROUP_ID = "nodex.GROUP_ID";
	public static final String GROUP_NAME = "nodex.GROUP_NAME";
	private static final Logger LOG =
			getLogger(BriarActivity.class.getName());
	@Inject
	BriarController briarController;
	@Deprecated
	@Inject
	DbController dbController;
	@Inject
	protected LockManager lockManager;
	@Inject
	AndroidWakeLockManager wakeLockManager;
	@Override
	public void onStart() {
		super.onStart();
		lockManager.onActivityStart();
	}
	@Override
	protected void onActivityResult(int request, int result,
			@Nullable Intent data) {
		super.onActivityResult(request, result, data);
		if (request == REQUEST_PASSWORD) {
			if (result == RESULT_OK) {
				if (LOG.isLoggable(INFO)) {
					LOG.info("Recreating " + getClass().getSimpleName()
							+ " after signing in");
				}
				recreate();
			}
		} else if (request == REQUEST_UNLOCK && result != RESULT_OK) {
			supportFinishAfterTransition();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if (!briarController.accountSignedIn() && !isFinishing()) {
			LOG.info("Not signed in, launching StartupActivity");
			Intent i = new Intent(this, StartupActivity.class);
			startActivityForResult(i, REQUEST_PASSWORD);
		} else if (lockManager.isLocked() && !isFinishing()) {
			LOG.info("Locked, launching UnlockActivity");
			Intent i = new Intent(this, UnlockActivity.class);
			startActivityForResult(i, REQUEST_UNLOCK);
		} else if (SDK_INT >= 23) {
			briarController.hasDozed(new UiResultHandler<Boolean>(this) {
				@Override
				public void onResultUi(Boolean result) {
					if (result) showDozeDialog(R.string.dnkm_warning_dozed_1);
				}
			});
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		lockManager.onActivityStop();
	}
	protected void setSceneTransitionAnimation(
			@Nullable Transition enterTransition,
			@Nullable Transition exitTransition,
			@Nullable Transition returnTransition) {
		if (isSamsung7()) {
			return;
		}
		if (enterTransition != null) excludeSystemUi(enterTransition);
		if (exitTransition != null) excludeSystemUi(exitTransition);
		if (returnTransition != null) excludeSystemUi(returnTransition);
		Window window = getWindow();
		window.setEnterTransition(enterTransition);
		window.setExitTransition(exitTransition);
		window.setReturnTransition(returnTransition);
	}
	protected Toolbar setUpCustomToolbar(boolean ownLayout) {
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowCustomEnabled(ownLayout);
			ab.setDisplayShowTitleEnabled(!ownLayout);
		}
		return toolbar;
	}
	protected void showDozeDialog(@StringRes int message) {
		MaterialAlertDialogBuilder b =
				new MaterialAlertDialogBuilder(this, R.style.BriarDialogTheme);
		b.setMessage(message);
		b.setView(R.layout.checkbox);
		b.setPositiveButton(R.string.fix,
				(dialog, which) -> {
					Intent i = getDozeWhitelistingIntent(BriarActivity.this);
					try {
						startActivityForResult(i, REQUEST_DOZE_WHITELISTING);
					} catch (ActivityNotFoundException e) {
						logException(LOG, WARNING, e);
						Toast.makeText(this, R.string.error_start_activity,
								LENGTH_LONG).show();
					}
					dialog.dismiss();
				});
		b.setNegativeButton(R.string.cancel,
				(dialog, which) -> dialog.dismiss());
		b.setOnDismissListener(dialog -> {
			CheckBox checkBox =
					((AlertDialog) dialog).findViewById(R.id.checkbox);
			if (checkBox.isChecked())
				briarController.doNotAskAgainForDozeWhiteListing();
		});
		b.show();
	}
	protected void signOut(boolean removeFromRecentApps,
			boolean deleteAccount) {
		wakeLockManager.runWakefully(() -> {
			if (briarController.accountSignedIn()) {
				briarController.signOut(result -> {
					Runnable exit = () -> exit(removeFromRecentApps);
					wakeLockManager.executeWakefully(exit,
							this::runOnUiThread, "SignOut");
				}, deleteAccount);
			} else {
				if (deleteAccount) briarController.deleteAccount();
				exit(removeFromRecentApps);
			}
		}, "SignOut");
	}
	@Wakeful
	private void exit(boolean removeFromRecentApps) {
		if (removeFromRecentApps) startExitActivity();
		else finishAndExit();
	}
	@Wakeful
	private void startExitActivity() {
		Intent i = new Intent(this, ExitActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK
				| FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| FLAG_ACTIVITY_NO_ANIMATION
				| FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}
	@Wakeful
	private void finishAndExit() {
		finishAndRemoveTask();
		LOG.info("Exiting");
		BriarApplication app = (BriarApplication) getApplication();
		if (!app.isInstrumentationTest()) System.exit(0);
	}
	@Deprecated
	public void runOnDbThread(Runnable task) {
		dbController.runOnDbThread(task);
	}
	@Deprecated
	protected void finishOnUiThread() {
		runOnUiThreadUnlessDestroyed(this::supportFinishAfterTransition);
	}
}