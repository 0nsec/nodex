package org.nodex.android.controller;
import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import org.nodex.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.android.NodexApplication;
import org.nodex.android.NodexService;
import org.nodex.android.NodexService.NodexServiceConnection;
import org.nodex.android.controller.handler.ResultHandler;
import org.nodex.api.android.DozeWatchdog;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.CallSuper;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.android.dontkillmelib.DozeUtils.needsDozeWhitelisting;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
@NotNullByDefault
public class NodexControllerImpl implements NodexController {
	private static final Logger LOG =
			getLogger(NodexControllerImpl.class.getName());
	public static final String DOZE_ASK_AGAIN = "dozeAskAgain";
	private final NodexServiceConnection serviceConnection;
	private final AccountManager accountManager;
	private final LifecycleManager lifecycleManager;
	private final Executor databaseExecutor;
	private final SettingsManager settingsManager;
	private final DozeWatchdog dozeWatchdog;
	private final AndroidWakeLockManager wakeLockManager;
	private final Activity activity;
	private boolean bound = false;
	@Inject
	NodexControllerImpl(NodexServiceConnection serviceConnection,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			@DatabaseExecutor Executor databaseExecutor,
			SettingsManager settingsManager,
			DozeWatchdog dozeWatchdog,
			AndroidWakeLockManager wakeLockManager,
			Activity activity) {
		this.serviceConnection = serviceConnection;
		this.accountManager = accountManager;
		this.lifecycleManager = lifecycleManager;
		this.databaseExecutor = databaseExecutor;
		this.settingsManager = settingsManager;
		this.dozeWatchdog = dozeWatchdog;
		this.wakeLockManager = wakeLockManager;
		this.activity = activity;
	}
	@Override
	@CallSuper
	public void onActivityCreate(Activity activity) {
		if (accountManager.hasDatabaseKey()) startAndBindService();
	}
	@Override
	public void onActivityStart() {
	}
	@Override
	public void onActivityStop() {
	}
	@Override
	@CallSuper
	public void onActivityDestroy() {
		unbindService();
	}
	@Override
	public void startAndBindService() {
		activity.startService(new Intent(activity, NodexService.class));
		bound = activity.bindService(new Intent(activity, NodexService.class),
				serviceConnection, 0);
	}
	@Override
	public boolean accountSignedIn() {
		return accountManager.hasDatabaseKey() &&
				lifecycleManager.getLifecycleState().isAfter(STARTING_SERVICES);
	}
	@Override
	public void hasDozed(ResultHandler<Boolean> handler) {
		NodexApplication app = (NodexApplication) activity.getApplication();
		if (app.isInstrumentationTest() || !dozeWatchdog.getAndResetDozeFlag()
				|| !needsDozeWhitelisting(activity)) {
			handler.onResult(false);
			return;
		}
		databaseExecutor.execute(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				handler.onResult(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	@Override
	public void doNotAskAgainForDozeWhiteListing() {
		databaseExecutor.execute(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(DOZE_ASK_AGAIN, false);
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	@Override
	public void signOut(ResultHandler<Void> handler, boolean deleteAccount) {
		wakeLockManager.executeWakefully(() -> {
			try {
				IBinder binder = serviceConnection.waitForBinder();
				NodexService service =
						((NodexService.NodexBinder) binder).getService();
				service.waitForStartup();
				LOG.info("Shutting down service");
				service.shutdown(true);
				service.waitForShutdown();
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for service");
			} finally {
				if (deleteAccount) accountManager.deleteAccount();
			}
			handler.onResult(null);
		}, "SignOut");
	}
	@Override
	public void deleteAccount() {
		accountManager.deleteAccount();
	}
	private void unbindService() {
		if (bound) activity.unbindService(serviceConnection);
	}
}