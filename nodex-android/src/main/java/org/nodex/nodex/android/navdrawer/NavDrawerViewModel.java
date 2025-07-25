package org.nodex.android.navdrawer;
import android.app.Application;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.NodexApplication;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.dontkillmelib.DozeUtils.needsDozeWhitelisting;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.TestingConstants.EXPIRY_DATE;
import static org.nodex.android.controller.NodexControllerImpl.DOZE_ASK_AGAIN;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
@NotNullByDefault
public class NavDrawerViewModel extends DbViewModel {
	private static final Logger LOG =
			getLogger(NavDrawerViewModel.class.getName());
	private static final String EXPIRY_DATE_WARNING = "expiryDateWarning";
	private static final String SHOW_TRANSPORTS_ONBOARDING =
			"showTransportsOnboarding";
	private final SettingsManager settingsManager;
	private final MutableLiveData<Boolean> showExpiryWarning =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> shouldAskForDozeWhitelisting =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> showTransportsOnboarding =
			new MutableLiveData<>();
	@Inject
	NavDrawerViewModel(Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
	}
	LiveData<Boolean> showExpiryWarning() {
		return showExpiryWarning;
	}
	@UiThread
	void checkExpiryWarning() {
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				int warningInt = settings.getInt(EXPIRY_DATE_WARNING, 0);
				if (warningInt == 0) {
					showExpiryWarning.postValue(true);
				} else {
					long warningLong = warningInt * 1000L;
					long now = System.currentTimeMillis();
					long daysSinceLastWarning =
							(now - warningLong) / DAYS.toMillis(1);
					long daysBeforeExpiry =
							(EXPIRY_DATE - now) / DAYS.toMillis(1);
					if (daysSinceLastWarning >= 30) {
						showExpiryWarning.postValue(true);
					} else if (daysBeforeExpiry <= 3 &&
							daysSinceLastWarning > 0) {
						showExpiryWarning.postValue(true);
					} else {
						showExpiryWarning.postValue(false);
					}
				}
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@UiThread
	void expiryWarningDismissed() {
		showExpiryWarning.setValue(false);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				int date = (int) (System.currentTimeMillis() / 1000L);
				settings.putInt(EXPIRY_DATE_WARNING, date);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<Boolean> shouldAskForDozeWhitelisting() {
		return shouldAskForDozeWhitelisting;
	}
	@UiThread
	void checkDozeWhitelisting() {
		NodexApplication app = getApplication();
		if (app.isInstrumentationTest() ||
				!needsDozeWhitelisting(getApplication())) {
			shouldAskForDozeWhitelisting.setValue(false);
			return;
		}
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				shouldAskForDozeWhitelisting.postValue(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				shouldAskForDozeWhitelisting.postValue(true);
			}
		});
	}
	@UiThread
	LiveData<Boolean> showTransportsOnboarding() {
		return showTransportsOnboarding;
	}
	@UiThread
	void checkTransportsOnboarding() {
		if (showTransportsOnboarding.getValue() != null) return;
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean show =
						settings.getBoolean(SHOW_TRANSPORTS_ONBOARDING, true);
				showTransportsOnboarding.postValue(show);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	@UiThread
	void transportsOnboardingShown() {
		showTransportsOnboarding.setValue(false);
		runOnDbThread(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(SHOW_TRANSPORTS_ONBOARDING, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
}