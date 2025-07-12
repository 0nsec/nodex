package org.nodex.android.splash;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.R;
import org.nodex.android.activity.ActivityComponent;
import org.nodex.android.activity.BaseActivity;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.lang.System.currentTimeMillis;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.BriarApplication.ENTRY_ACTIVITY;
import static org.nodex.android.TestingConstants.EXPIRY_DATE;
import static org.nodex.android.TestingConstants.IS_DEBUG_BUILD;
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SplashScreenActivity extends BaseActivity {
	private static final Logger LOG =
			getLogger(SplashScreenActivity.class.getName());
	@Inject
	protected AccountManager accountManager;
	@Inject
	protected AndroidExecutor androidExecutor;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}
	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		getWindow().setExitTransition(new Fade());
		setPreferencesDefaults();
		setContentView(R.layout.splash);
		if (accountManager.hasDatabaseKey()) {
			startNextActivity(ENTRY_ACTIVITY);
			finish();
		} else {
			int duration =
					getResources().getInteger(R.integer.splashScreenDuration);
			new Handler().postDelayed(() -> {
				if (IS_DEBUG_BUILD && currentTimeMillis() >= EXPIRY_DATE) {
					LOG.info("Expired");
					startNextActivity(ExpiredActivity.class);
				} else {
					startNextActivity(ENTRY_ACTIVITY);
				}
				supportFinishAfterTransition();
			}, duration);
		}
	}
	private void startNextActivity(Class<? extends Activity> activityClass) {
		Intent i = new Intent(this, activityClass);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
	private void setPreferencesDefaults() {
		androidExecutor.runOnBackgroundThread(
				() -> setDefaultValues(SplashScreenActivity.this,
						R.xml.panic_preferences, false));
	}
	@Override
	public boolean shouldAllowTap() {
		return true;
	}
}